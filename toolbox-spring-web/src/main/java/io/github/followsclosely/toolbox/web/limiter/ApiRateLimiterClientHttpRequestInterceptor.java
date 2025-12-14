package io.github.followsclosely.toolbox.web.limiter;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * A ClientHttpRequestInterceptor that enforces API rate limiting
 * using the provided ApiRateLimiter before executing the request.
 */
@Slf4j
@RequiredArgsConstructor
public class ApiRateLimiterClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final @NonNull ApiRateLimiter rateLimiter;

    public ApiRateLimiterClientHttpRequestInterceptor(ApiRateLimiterConfiguration configuration) {
        rateLimiter = new GenericApiRateLimiter(configuration);
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        rateLimiter.waitAsNeeded();
        try {
            return execution.execute(request, body);
        } finally {
            rateLimiter.resetLastCallTime();
        }
    }

}