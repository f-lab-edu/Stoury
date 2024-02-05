package com.stoury.utils;


import java.time.temporal.ChronoUnit;

public enum CacheKeys {
    POPULAR_DOMESTIC_SPOTS("PopularSpots:Domestic"),
    POPULAR_ABROAD_SPOTS("PopularSpots:International"),
    DAILY_HOT_FEEDS("HotFeeds:" + ChronoUnit.DAYS),
    WEEKLY_HOT_FEEDS("HotFeeds:" + ChronoUnit.WEEKS),
    MONTHLY_HOT_FEEDS("HotFeeds:" + ChronoUnit.MONTHS);

    private final String key;
    public static final String LIKERS_KEY_PREFIX = "Likers:";
    public static final String COUNT_SNAPSHOT_KEY_PREFIX = "LikesCount:";


    CacheKeys(String key) {
        this.key = key;
    }

    public static CacheKeys getHotFeedsKey(ChronoUnit chronoUnit) {
        return switch (chronoUnit) {
            case DAYS -> DAILY_HOT_FEEDS;
            case WEEKS -> WEEKLY_HOT_FEEDS;
            case MONTHS -> MONTHLY_HOT_FEEDS;
            default -> throw new IllegalArgumentException();
        };
    }

    public static String getLikersKey(String feedId) {
        return LIKERS_KEY_PREFIX + feedId;
    }

    public static String getCountSnapshotKey(ChronoUnit chronoUnit, String feedId) {
        return COUNT_SNAPSHOT_KEY_PREFIX + feedId + ":" + chronoUnit;
    }
}
