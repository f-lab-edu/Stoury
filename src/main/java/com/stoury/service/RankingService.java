package com.stoury.service;

import com.stoury.dto.diary.SimpleDiaryResponse;
import com.stoury.dto.feed.SimpleFeedResponse;
import com.stoury.repository.RankingRepository;
import com.stoury.utils.cachekeys.PopularSpotsKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.stoury.utils.cachekeys.HotFeedsKeys.getHotFeedsKey;

@Service
@RequiredArgsConstructor
public class RankingService {
    private final RankingRepository rankingRepository;

    public List<SimpleFeedResponse> getHotFeeds(ChronoUnit chronoUnit) {
        return rankingRepository.getRankedFeeds(getHotFeedsKey(chronoUnit));
    }


    public List<String> getPopularAbroadSpots() {
        return rankingRepository.getRankedLocations(PopularSpotsKey.POPULAR_ABROAD_SPOTS);
    }

    public List<String> getPopularDomesticSpots() {
        return rankingRepository.getRankedLocations(PopularSpotsKey.POPULAR_DOMESTIC_SPOTS);
    }

    public List<SimpleDiaryResponse> getHotDiaries() {
        return rankingRepository.getRankedDiaries();
    }
}
