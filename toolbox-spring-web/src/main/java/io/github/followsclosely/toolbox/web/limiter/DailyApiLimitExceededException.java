package io.github.followsclosely.toolbox.web.limiter;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DailyApiLimitExceededException extends IOException {

    public DailyApiLimitExceededException(int callsMade, int dailyLimit, ZonedDateTime resetsAt) {
        super(String.format(
                "Daily API limit of %d calls exceeded (%d calls made today). Resets at %s.",
                dailyLimit, callsMade, resetsAt.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
        ));
    }
}
