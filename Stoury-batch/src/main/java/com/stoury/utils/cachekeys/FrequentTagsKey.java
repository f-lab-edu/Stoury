package com.stoury.utils.cachekeys;

public class FrequentTagsKey {
    public static final String FREQUENT_TAGS_KEY = "FrequentTags:";

    public static String getFrequentTagsKey(String memberId) {
        return FREQUENT_TAGS_KEY + memberId;
    }
}
