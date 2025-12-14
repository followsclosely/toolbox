package io.github.followsclosely.toolbox.web.limiter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericApiRateLimiterTest {

    @Test
    void testDefaultConstructorAndWaitAsNeeded() {
        GenericApiRateLimiter limiter = new GenericApiRateLimiter();
        limiter.resetLastCallTime();
        long start = System.currentTimeMillis();
        limiter.waitAsNeeded();
        long elapsed = System.currentTimeMillis() - start;
        // Should wait at least MIN_DELAY_MS (1000ms) minus a small margin for timing imprecision
        assertTrue(elapsed >= GenericApiRateLimiter.MIN_DELAY_MS - 50, "Should wait at least the minimum delay");
    }

    @Test
    void testCustomDelay() {
        long customDelay = 200;
        GenericApiRateLimiter limiter = new GenericApiRateLimiter(customDelay, 0);
        limiter.resetLastCallTime();
        long start = System.currentTimeMillis();
        limiter.waitAsNeeded();
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= customDelay - 20, "Should wait at least the custom delay");
    }

    @Test
    void testBorrowIncreasesWait() {
        long customDelay = 100;
        GenericApiRateLimiter limiter = new GenericApiRateLimiter(customDelay, 0);
        limiter.resetLastCallTime();
        limiter.borrow(300);
        long start = System.currentTimeMillis();
        limiter.waitAsNeeded();
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= 400 - 20, "Should wait at least the sum of delay and borrowed time");
    }

    @Test
    void testConfigurationConstructor() {
        ApiRateLimiterConfiguration config = new ApiRateLimiterConfiguration();
        config.setMinWaitMsBetweenCalls(150);
        config.setRandomMsAddition(0);
        GenericApiRateLimiter limiter = new GenericApiRateLimiter(config);
        limiter.resetLastCallTime();
        long start = System.currentTimeMillis();
        limiter.waitAsNeeded();
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= 150 - 20, "Should wait at least the configured delay");
    }
}