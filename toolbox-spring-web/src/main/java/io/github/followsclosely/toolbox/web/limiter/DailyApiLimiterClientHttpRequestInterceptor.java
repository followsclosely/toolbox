package io.github.followsclosely.toolbox.web.limiter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DailyApiLimiterClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final DailyApiLimitConfiguration config;
    private final Clock clock;
    private volatile LocalDate currentDay;
    private final AtomicInteger callsToday = new AtomicInteger(0);

    public DailyApiLimiterClientHttpRequestInterceptor(DailyApiLimitConfiguration config) {
        this(config, Clock.systemUTC());
    }

    DailyApiLimiterClientHttpRequestInterceptor(DailyApiLimitConfiguration config, Clock clock) {
        this.config = config;
        this.clock = clock;
        this.currentDay = LocalDate.now(clock);
    }

    public int getCallsToday(){
        return callsToday.get();
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        synchronized (this) {
            LocalDate today = LocalDate.now(clock);
            if (!today.equals(currentDay)) {
                log.info("Daily API call counter reset for new day: {}", today);
                currentDay = today;
                callsToday.set(0);
            }

            int current = callsToday.get();
            if (current >= config.getMaxCallsPerDay()) {
                ZonedDateTime resetsAt = today.plusDays(1).atStartOfDay(ZoneOffset.UTC);
                throw new DailyApiLimitExceededException(current, config.getMaxCallsPerDay(), resetsAt);
            }

            if (current >= (int) (config.getMaxCallsPerDay() * 0.9)) {
                log.warn("Daily API limit approaching: {}/{} calls used today", current + 1, config.getMaxCallsPerDay());
            }

            callsToday.incrementAndGet();
        }

        return execution.execute(request, body);
    }
}
