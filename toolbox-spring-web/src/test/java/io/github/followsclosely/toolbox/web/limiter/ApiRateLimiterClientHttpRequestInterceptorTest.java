package io.github.followsclosely.toolbox.web.limiter;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class ApiRateLimiterClientHttpRequestInterceptorTest {

    @Test
    void intercept_invokesRateLimiterAndExecutesRequest() throws IOException {
        ApiRateLimiter rateLimiter = mock(ApiRateLimiter.class);
        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        byte[] body = new byte[0];

        when(execution.execute(request, body)).thenReturn(response);

        ApiRateLimiterClientHttpRequestInterceptor interceptor = new ApiRateLimiterClientHttpRequestInterceptor(rateLimiter);
        ClientHttpResponse actual = interceptor.intercept(request, body, execution);

        verify(rateLimiter, times(1)).waitAsNeeded();
        verify(rateLimiter, times(1)).resetLastCallTime();
        verify(execution, times(1)).execute(request, body);
        assertSame(response, actual);
    }
}