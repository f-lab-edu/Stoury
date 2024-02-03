package com.stoury.service;

import com.stoury.dto.LocationResponse;
import org.springframework.stereotype.Service;

@Service
public class GeocodingService {
    // TODO: 지오코딩 api를 이용해 구현 필요
    public LocationResponse getLocationFrom(double latitude, double longitude) {
        return new LocationResponse("default_city", "default_country");
    }
}
