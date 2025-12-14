package io.github.followsclosely.toolbox.web.limiter;

import lombok.Data;

@Data
public class ApiRateLimiterConfiguration {
    private boolean enabled = true;
    private long minWaitMsBetweenCalls = 1000;
    private long randomMsAddition = 50;
}
