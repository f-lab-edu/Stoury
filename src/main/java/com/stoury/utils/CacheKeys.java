package com.stoury.utils;


public enum CacheKeys {
    POPULAR_DOMESTIC_SPOTS("POPULAR_DOMESTIC"),
    POPULAR_ABROAD_SPOTS("POPULAR_ABROAD");

    private String key;

    CacheKeys(String key) {
        this.key = key;
    }
}
