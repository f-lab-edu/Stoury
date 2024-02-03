package com.stoury.service.location;

import com.stoury.dto.LocationResponse;

public interface LocationService {
    LocationResponse getLocationFrom(double latitude, double longitude);
}
