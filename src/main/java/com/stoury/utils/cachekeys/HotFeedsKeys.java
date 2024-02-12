package com.stoury.utils.cachekeys;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;

public enum HotFeedsKeys {
    DAILY_HOT_FEEDS(ChronoUnit.DAYS),
    WEEKLY_HOT_FEEDS(ChronoUnit.WEEKS),
    MONTHLY_HOT_FEEDS(ChronoUnit.MONTHS);
    private final String key;
    private final ChronoUnit chronoUnit;

    HotFeedsKeys(ChronoUnit chronoUnit) {
        this.key = "HotFeeds:" + chronoUnit;
        this.chronoUnit = chronoUnit;
    }

    public static HotFeedsKeys getHotFeedsKey(ChronoUnit chronoUnit) {
        return Arrays.stream(HotFeedsKeys.values())
                .filter(hotFeedsKey -> hotFeedsKey.chronoUnit.equals(chronoUnit))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
