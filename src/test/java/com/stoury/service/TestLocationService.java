package com.stoury.service;

import com.stoury.service.location.LocationService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class TestLocationService implements LocationService {
    @Override
    public void setLocation(Long feedId, double latitude, double longitude) {
    }
}
