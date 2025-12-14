package io.github.followsclosely.toolbox.web.limiter;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;


@Slf4j
public final class GenericApiRateLimiter implements ApiRateLimiter {

    /**
     * Default singleton instance with standard delay settings. This is convenient for general use cases.
     */
    public static final GenericApiRateLimiter DEFAULT_INSTANCE = new GenericApiRateLimiter();

    /**
     * The minimum delay required between calls, in milliseconds.
     */
    public static final long MIN_DELAY_MS = 1000;

    /**
     * Minimum delay enforced between API calls, in milliseconds.
     */
    private final long minDelay;
    /**
     * Maximum random bonus added to the delay, in milliseconds.
     */
    private final long minDelayBonus;

    /**
     * Timestamp of the last API call, in milliseconds since epoch.
     */
    private final AtomicLong lastCallTime = new AtomicLong(0);

    /**
     * Additional milliseconds borrowed to be added to the next wait time.
     */
    private final AtomicLong borrowedMillis = new AtomicLong(0);

    /**
     * Total number of API calls made through this rate limiter.
     */
    private final AtomicLong totalCallsMade = new AtomicLong(0);

    /**
     * Constructs a rate limiter with the default minimum delay.
     */
    public GenericApiRateLimiter() {
        this(MIN_DELAY_MS);
    }

    /**
     * Constructs a rate limiter with a custom minimum delay and default bonus (10ms).
     *
     * @param minimumDelay Minimum delay between calls in milliseconds.
     */
    public GenericApiRateLimiter(long minimumDelay) {
        this(minimumDelay, 10);
    }

    /**
     * Constructs a rate limiter using the provided configuration.
     *
     * @param configuration The API limits configuration.
     * @see ApiRateLimiterConfiguration
     */
    public GenericApiRateLimiter(ApiRateLimiterConfiguration configuration) {
        this(configuration.getMinWaitMsBetweenCalls(), configuration.getRandomMsAddition());
    }

    /**
     * Constructs a rate limiter with custom minimum delay and bonus.
     *
     * @param minDelay      Minimum delay between calls in milliseconds.
     * @param minDelayBonus Maximum random bonus added to delay in milliseconds.
     */
    public GenericApiRateLimiter(long minDelay, long minDelayBonus) {
        this.minDelay = minDelay;
        this.minDelayBonus = minDelayBonus;
    }

    /**
     * Borrows additional milliseconds to be added to the next wait time.
     * This can be used to dynamically adjust the wait time based on
     * external factors or specific API requirements.
     *
     * @param millis The number of milliseconds to borrow.
     */
    public void borrow(long millis) {
        totalCallsMade.incrementAndGet();
        borrowedMillis.addAndGet(millis);
    }

    /**
     * Waits for at least the configured minimum delay since the last call.
     * If the required time has not elapsed, the current thread will sleep until it has.
     * A random bonus delay may be added to reduce the risk of hitting rate limits.
     * Thread-safe via AtomicLong fields.
     */
    public void waitAsNeeded() {
        totalCallsMade.incrementAndGet();
        long currentTime = System.currentTimeMillis();
        long timeSinceLastCall = currentTime - lastCallTime.get();

        long delayNeeded = minDelay + borrowedMillis.get();
        if (timeSinceLastCall < delayNeeded) {
            long timeToWait = delayNeeded - timeSinceLastCall;
            try {
                long timeToWaitPlus = (timeToWait + ((long) (Math.random() * minDelayBonus)));
                log.info("Call-{}: Need to wait {}ms, but waiting for {}ms to enforce the {}ms delay (plus {}ms)...", totalCallsMade.get(), timeToWait, timeToWaitPlus, minDelay, timeToWaitPlus - timeToWait);
                Thread.sleep(timeToWaitPlus);
                borrowedMillis.set(0);
                lastCallTime.set(System.currentTimeMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("The wait was interrupted.");
            }
        }

    }

    /**
     * Resets the last call time to the current system time.
     * This can be used to indicate that a call has just been made,
     * effectively starting the wait timer anew.
     */
    public void resetLastCallTime() {
        lastCallTime.getAndSet(System.currentTimeMillis());
    }
}
