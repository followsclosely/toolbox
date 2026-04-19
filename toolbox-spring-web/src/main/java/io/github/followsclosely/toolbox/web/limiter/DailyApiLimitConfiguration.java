package io.github.followsclosely.toolbox.web.limiter;

import lombok.Data;

@Data
public class DailyApiLimitConfiguration {
    private boolean enabled = true;
    private int maxCallsPerDay = 5000;
}
