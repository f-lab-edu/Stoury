package com.stoury.service.location;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.*;
import com.stoury.dto.LocationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

@Service
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class GeocodingLocationService implements LocationService{
    private final GeoApiContext context;
    @Override
    public LocationResponse getLocationFrom(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        GeocodingApiRequest req = GeocodingApi.reverseGeocode(context, latLng)
                .locationType(LocationType.APPROXIMATE);

        try {
            GeocodingResult[] results = req.await();

            return new LocationResponse(getCity(results), getCountry(results));
        } catch (ApiException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getCity(GeocodingResult[] results) {
        return Arrays.stream(results)
                .flatMap(res -> Arrays.stream(res.addressComponents))
                .filter(component -> Arrays.asList(component.types).contains(AddressComponentType.LOCALITY))
                .map(component -> component.longName)
                .findFirst()
                .orElse("UNDEFINED");
    }

    private String getCountry(GeocodingResult[] results) {
        return Arrays.stream(results)
                .flatMap(res -> Arrays.stream(res.addressComponents))
                .filter(component -> Arrays.asList(component.types).contains(AddressComponentType.COUNTRY))
                .map(component -> component.longName)
                .findFirst()
                .orElse("UNDEFINED");
    }
}
