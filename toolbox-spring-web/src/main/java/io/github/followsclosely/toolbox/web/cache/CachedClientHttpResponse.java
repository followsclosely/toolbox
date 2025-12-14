package io.github.followsclosely.toolbox.web.cache;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Slf4j
public class CachedClientHttpResponse implements ClientHttpResponse {

    @Getter
    private final HttpStatusCode statusCode;
    @Getter
    private final HttpHeaders headers;
    private final byte[] body;

    // Simple constructor for cache hits (assume 200 OK)
    public CachedClientHttpResponse(byte[] body) {
        this(HttpStatus.OK, new HttpHeaders(), body);
    }

    // Full constructor for real responses
    public CachedClientHttpResponse(HttpStatusCode statusCode, HttpHeaders headers, byte[] body) {
        this.statusCode = statusCode;
        this.headers = HttpHeaders.readOnlyHttpHeaders(headers); // Make immutable if needed
        this.body = body != null ? body : new byte[0];
    }

    @Override
    public String getStatusText() {
        return HttpStatus.valueOf(statusCode.value()).getReasonPhrase();
    }

    @Override
    public void close() {
    }

    @Override
    public InputStream getBody() {
        return new ByteArrayInputStream(body);
    }

}