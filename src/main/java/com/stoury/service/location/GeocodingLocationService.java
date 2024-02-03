package com.stoury.service.location;

import com.stoury.dto.LocationResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
public class GeocodingLocationService implements LocationService{
    @Override
    public LocationResponse getLocationFrom(double latitude, double longitude) {
        return new LocationResponse("default_city", "default_country");
    }
}
