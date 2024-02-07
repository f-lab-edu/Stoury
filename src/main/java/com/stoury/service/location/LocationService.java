package com.stoury.service.location;

import com.stoury.dto.feed.LocationResponse;

public interface LocationService {
    LocationResponse getLocation(double latitude, double longitude);
}
