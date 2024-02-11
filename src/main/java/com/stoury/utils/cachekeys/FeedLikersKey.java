package com.stoury.utils.cachekeys;

public class FeedLikersKey {
    public static final String LIKERS_KEY_PREFIX = "Likers:";
    public static String getLikersKey(String feedId) {
        return LIKERS_KEY_PREFIX + feedId;
    }
}
