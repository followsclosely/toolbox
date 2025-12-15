package io.github.followsclosely.toolbox.web.cache;

import io.github.followsclosely.toolbox.web.limiter.ApiRateLimiter;
import io.github.followsclosely.toolbox.web.limiter.ApiRateLimiterConfiguration;
import io.github.followsclosely.toolbox.web.limiter.GenericApiRateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.DigestUtils;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * A ClientHttpRequestInterceptor that caches HTTP responses on disk.
 * It saves both the response body and key headers to files in a specified cache directory.
 * On subsequent requests, it checks for cached responses and serves them if available.
 * Optionally integrates with an ApiRateLimiter to manage request rates.
 */
@Slf4j
public class DiskCachingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final Path cacheDir;
    private final ApiRateLimiter rateLimiter;

    public DiskCachingClientHttpRequestInterceptor(String cacheDirectory) {
        this(cacheDirectory, null);
    }

    public DiskCachingClientHttpRequestInterceptor(DiskCachingConfiguration configuration) {
        this(configuration.getDirectory(), null);
    }

    public DiskCachingClientHttpRequestInterceptor(
            DiskCachingConfiguration diskConfig,
            ApiRateLimiterConfiguration rateConfig) {
        this(diskConfig.getDirectory(), new GenericApiRateLimiter(rateConfig));
    }

    public DiskCachingClientHttpRequestInterceptor(String cacheDirectory, ApiRateLimiter rateLimiter) {
        this.cacheDir = Paths.get(cacheDirectory);
        this.rateLimiter = rateLimiter;
        try {
            Files.createDirectories(this.cacheDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create cache directory: " + cacheDirectory, e);
        }
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        String cacheKey = createCacheKey(request);
        Path bodyFile = cacheDir.resolve(cacheKey + "-body.json");
        Path headersFile = cacheDir.resolve(cacheKey + "-headers.properties");

        // Cache HIT: load body + headers from disk
        if (Files.exists(bodyFile) && Files.exists(headersFile)) {
            log.info("Cache HIT (disk): {} {}", request.getMethod(), request.getURI());

            byte[] cachedBody = Files.readAllBytes(bodyFile);

            Properties headerProps = new Properties();
            try (InputStream is = Files.newInputStream(headersFile)) {
                headerProps.load(is);
            }

            HttpHeaders headers = new HttpHeaders();
            headerProps.forEach((k, v) -> headers.add((String) k, (String) v));

            return new CachedClientHttpResponse(HttpStatusCode.valueOf(200), headers, cachedBody);
        }

        Files.createDirectories(bodyFile.getParent());

        // If there is a rate limiter, wait as needed before making real request
        if (rateLimiter != null) {
            rateLimiter.waitAsNeeded();
        }

        // Cache MISS: real request
        log.info("Cache MISS: {} {}", request.getMethod(), request.getURI());
        ClientHttpResponse realResponse = execution.execute(request, body);

        // Read body once
        byte[] responseBodyBytes;
        try (InputStream is = realResponse.getBody()) {
            responseBodyBytes = StreamUtils.copyToByteArray(is);
        }

        // Save body
        Files.write(bodyFile, responseBodyBytes);

        // Save key headers (Content-Type is crucial; add others if needed)
        Properties headerProps = getProperties(realResponse);
        // Add more headers if your API uses them (e.g., Cache-Control, ETag)

        try (OutputStream os = Files.newOutputStream(headersFile)) {
            headerProps.store(os, "Cached response headers");
        }

        log.info("Saved response to disk (body + headers)");

        // If there is a rate limiter, reset last call time after the real request
        if (rateLimiter != null) {
            rateLimiter.resetLastCallTime();
        }

        // Return real response (with original headers)
        return new CachedClientHttpResponse(
                realResponse.getStatusCode(),
                realResponse.getHeaders(),
                responseBodyBytes
        );
    }

    private Properties getProperties(ClientHttpResponse realResponse) {
        Properties headerProps = new Properties();
        HttpHeaders origHeaders = realResponse.getHeaders();
        String contentType = origHeaders.getFirst(HttpHeaders.CONTENT_TYPE);
        if (contentType != null) {
            headerProps.setProperty(HttpHeaders.CONTENT_TYPE, contentType);
        }
        String contentLength = origHeaders.getFirst(HttpHeaders.CONTENT_LENGTH);
        if (contentLength != null) {
            headerProps.setProperty(HttpHeaders.CONTENT_LENGTH, contentLength);
        }
        return headerProps;
    }

    private String createCacheKey(HttpRequest request) {
        String hint = DiskCachingHint.get();
        if (hint != null && !hint.isBlank()) {
            return hint;
        }
        String rawKey = request.getMethod() + " " + request.getURI();
        return DigestUtils.md5DigestAsHex(rawKey.getBytes(StandardCharsets.UTF_8));
    }
}