package com.stoury.utils.cachekeys;

import java.time.temporal.ChronoUnit;

public enum HotFeedsKeys {
    DAILY_HOT_FEEDS("HotFeeds:" + ChronoUnit.DAYS),
    WEEKLY_HOT_FEEDS("HotFeeds:" + ChronoUnit.WEEKS),
    MONTHLY_HOT_FEEDS("HotFeeds:" + ChronoUnit.MONTHS);
    private final String key;
    HotFeedsKeys(String key) {
        this.key = key;
    }

    public static HotFeedsKeys getHotFeedsKey(ChronoUnit chronoUnit) {
        return switch (chronoUnit) {
            case DAYS -> DAILY_HOT_FEEDS;
            case WEEKS -> WEEKLY_HOT_FEEDS;
            case MONTHS -> MONTHLY_HOT_FEEDS;
            default -> throw new IllegalArgumentException();
        };
    }
}
