package com.stoury.utils.cachekeys;

public class RecommendFeedsKey {
    public static final String RECOMMEND_FEEDS_KEY = "RecommendFeeds:";
    public static String getRecommendFeedsKey(String memberId){
        return RECOMMEND_FEEDS_KEY + memberId;
    }
}
