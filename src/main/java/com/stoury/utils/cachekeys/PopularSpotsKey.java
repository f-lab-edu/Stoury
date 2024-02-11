package com.stoury.utils.cachekeys;

public enum PopularSpotsKey {
    POPULAR_DOMESTIC_SPOTS("PopularSpots:Domestic"),
    POPULAR_ABROAD_SPOTS("PopularSpots:International");
    private final String key;


    PopularSpotsKey(String key) {
        this.key = key;
    }
}
