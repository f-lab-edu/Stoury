package com.stoury.scheduler;

import com.stoury.repository.FeedRepository;
import com.stoury.repository.RankingRepository;
import com.stoury.utils.CacheKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Scheduler {
    private final FeedRepository feedRepository;
    private final RankingRepository rankingRepository;
    @Scheduled(fixedRate = 12 * 60 * 60 * 1000)
    @Transactional
    public void updatePopularSpotsCache() {
        Pageable pageable = PageRequest.of(0, 10);
        List<String> rankedDomesticCities = feedRepository.findTop10CitiesInKorea(pageable);
        rankingRepository.update(CacheKeys.POPULAR_DOMESTIC_SPOTS, rankedDomesticCities);

        List<String> rankedCountries = feedRepository.findTop10CountriesNotKorea(pageable);
        rankingRepository.update(CacheKeys.POPULAR_ABROAD_SPOTS, rankedCountries);
    }
}
