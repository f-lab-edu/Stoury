package com.stoury.utils.cachekeys;

import java.time.temporal.ChronoUnit;

public class FeedLikesCountSnapshotKeys {
    public static final String COUNT_SNAPSHOT_KEY_PREFIX = "LikesCount:";

    public static String getCountSnapshotKey(ChronoUnit chronoUnit, String feedId) {
        return COUNT_SNAPSHOT_KEY_PREFIX + feedId + ":" + chronoUnit;
    }
}
