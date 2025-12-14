package io.github.followsclosely.toolbox.web.cache;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CachedClientHttpResponseTest {

    @Test
    void testSimpleConstructorAndGetters() throws IOException {
        byte[] body = "hello world".getBytes(StandardCharsets.UTF_8);
        CachedClientHttpResponse response = new CachedClientHttpResponse(body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(HttpStatus.OK.getReasonPhrase(), response.getStatusText());
        assertNotNull(response.getHeaders());
        assertTrue(response.getHeaders().isEmpty());
        try (InputStream is = response.getBody()) {
            assertArrayEquals(body, is.readAllBytes());
        }
    }

    @Test
    void testFullConstructorAndGetters() throws IOException {
        byte[] body = "test body".getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Test", "value");
        CachedClientHttpResponse response = new CachedClientHttpResponse(HttpStatus.NOT_FOUND, headers, body);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), response.getStatusText());
        assertEquals("value", response.getHeaders().getFirst("X-Test"));
        try (InputStream is = response.getBody()) {
            assertArrayEquals(body, is.readAllBytes());
        }
    }

    @Test
    void testNullBodyDefaultsToEmpty() throws IOException {
        CachedClientHttpResponse response = new CachedClientHttpResponse(HttpStatus.OK, new HttpHeaders(), null);
        try (InputStream is = response.getBody()) {
            assertArrayEquals(new byte[0], is.readAllBytes());
        }
    }
}