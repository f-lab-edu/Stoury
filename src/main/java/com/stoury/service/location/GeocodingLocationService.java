package com.stoury.service.location;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.LocationType;
import com.stoury.dto.LocationResponse;
import com.stoury.exception.location.GeocodeApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

@Service
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class GeocodingLocationService implements LocationService {
    public static final String UNDEFINED_LOCATION = "UNDEFINED";
    private final GeoApiContext context;
    private static final AddressComponentType[] administrativeSteps = {
            AddressComponentType.LOCALITY,
            AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_4,
            AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_3,
            AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_2,
    };

    @Override
    @Transactional
    public LocationResponse getLocation(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        GeocodingApiRequest req = GeocodingApi.reverseGeocode(context, latLng)
                .locationType(LocationType.APPROXIMATE);

        GeocodingResult[] result;

        try {
            result = req.await();
        } catch (IOException | ApiException e) {
            throw new GeocodeApiException("Cannot load location information.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GeocodeApiException( "Api communication was interrupted.", e);
        }

        String city = getCity(result);
        String country = getCountry(result);

        return new LocationResponse(city, country);
    }

    private String getCity(GeocodingResult[] results) {
        for (AddressComponentType addressComponentType : administrativeSteps) {
            String city = getAddressComponent(results, addressComponentType);
            if (!UNDEFINED_LOCATION.equals(city)) {
                return city;
            }
        }
        return UNDEFINED_LOCATION;
    }

    private String getCountry(GeocodingResult[] results) {
        return getAddressComponent(results, AddressComponentType.COUNTRY);
    }

    private String getAddressComponent(GeocodingResult[] results, AddressComponentType type) {
        return Arrays.stream(results)
                .flatMap(res -> Arrays.stream(res.addressComponents))
                .filter(component -> Set.of(component.types).contains(type))
                .map(component -> component.longName)
                .findFirst()
                .orElse(UNDEFINED_LOCATION);
    }
}
