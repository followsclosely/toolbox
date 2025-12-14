package io.github.followsclosely.toolbox.web.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiskCachingClientHttpRequestInterceptorTest {
    private Path tempDir;
    private DiskCachingClientHttpRequestInterceptor interceptor;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("cache-test");
        interceptor = new DiskCachingClientHttpRequestInterceptor(tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        // Use try-with-resources for Files.walk
        try (var walk = Files.walk(tempDir)) {
            walk.map(Path::toFile)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(f -> {
                        if (!f.delete()) {
                            f.deleteOnExit();
                        }
                    });
        }
    }

    @Test
    void testCacheMissAndHit() throws IOException, URISyntaxException {
        // Mocks for request/response
        HttpRequest request = mock(HttpRequest.class);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(new URI("http://example.com/api/data"));

        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        byte[] responseBody = "response-data".getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(responseBody.length));

        when(response.getBody()).thenReturn(new ByteArrayInputStream(responseBody));
        when(response.getHeaders()).thenReturn(headers);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(execution.execute(eq(request), any())).thenReturn(response);

        // First call: should be a cache miss (writes to disk)
        try (ClientHttpResponse result1 = interceptor.intercept(request, new byte[0], execution)) {
            assertNotNull(result1);
            assertEquals(HttpStatus.OK, result1.getStatusCode());
            assertEquals("application/json", result1.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
            assertArrayEquals(responseBody, StreamUtils.copyToByteArray(result1.getBody()));
        }
        verify(execution, times(1)).execute(eq(request), any());

        // Second call: should be a cache hit (reads from disk, does not call execution)
        reset(execution);
        try (ClientHttpResponse result2 = interceptor.intercept(request, new byte[0], execution)) {
            assertNotNull(result2);
            assertEquals(HttpStatus.OK, result2.getStatusCode());
            assertEquals("application/json", result2.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
            assertArrayEquals(responseBody, StreamUtils.copyToByteArray(result2.getBody()));
        }
        verify(execution, never()).execute(any(), any());
    }
}