package io.github.followsclosely.toolbox.web.limiter;

/**
 * Interface for implementing rate limiting logic for API calls.
 * <p>
 * Implementations of this interface are responsible for enforcing a minimum delay between API calls,
 * optionally allowing dynamic adjustment of the delay and providing thread-safe mechanisms for controlling call rates.
 * </p>
 * <p>
 * Typical usage:
 * <pre>
 *     RebrkApiRateLimiter limiter = ...;
 *     limiter.waitAsNeeded(); // Enforces rate limit before making an API call
 * </pre>
 * </p>
 */
public interface ApiRateLimiter {
    /**
     * Borrows additional milliseconds to be added to the next wait time.
     * This can be used to dynamically adjust the wait time based on
     * external factors or specific API requirements.
     *
     * @param millis The number of milliseconds to borrow for the next wait.
     */
    void borrow(long millis);

    /**
     * Waits for at least the minimum required delay since the last time this method was called.
     * If the required time has not elapsed, the current thread will sleep until it has.
     * Implementations should ensure thread-safe updates to internal state and prevent
     * multiple threads from concurrently calculating the wait time.
     */
    void waitAsNeeded();

    /**
     * Resets the last call time to the current system time.
     * This can be used to indicate that a call has just been made,
     * effectively starting the wait timer anew.
     *
     * @return The new last call time in milliseconds since epoch.
     */
    void resetLastCallTime();
}
