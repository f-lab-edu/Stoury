package com.stoury.utils.cachekeys;

public class ViewedFeedsKey {
    public static final String VIEWED_FEEDS_KEY = "ViewedFeed:";
    public static String getViewedFeedsKey(String memberId){
        return VIEWED_FEEDS_KEY + memberId;
    }
}
