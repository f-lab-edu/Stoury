package com.stoury.service.location;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.PendingResult;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.LocationType;
import com.stoury.domain.Feed;
import com.stoury.exception.feed.FeedSearchException;
import com.stoury.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Set;

@Service
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class GeocodingLocationService implements LocationService{
    private final GeoApiContext context;
    private final FeedRepository feedRepository;

    @Override
    @Transactional
    public void setLocation(Long feedId, double latitude, double longitude) {
        Feed feed = feedRepository.findById(feedId).orElseThrow(FeedSearchException::new);
        LatLng latLng = new LatLng(latitude, longitude);
        GeocodingApiRequest req = GeocodingApi.reverseGeocode(context, latLng)
                .locationType(LocationType.APPROXIMATE);

        req.setCallback(new PendingResult.Callback<>() {
            @Override
            public void onResult(GeocodingResult[] result) {
                feed.updateLocation(getCity(result), getCountry(result));
                feedRepository.save(feed);
            }

            @Override
            public void onFailure(Throwable e) {
                log.info(e.getMessage());
            }
        });
    }

    private String getCity(GeocodingResult[] results) {
        return getAddressComponent(results, AddressComponentType.LOCALITY);
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
                .orElse("UNDEFINED");
    }
}
