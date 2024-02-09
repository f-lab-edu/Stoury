package com.stoury.service;

import com.stoury.dto.feed.FeedResponse;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.LikeRepository;
import com.stoury.repository.RankingRepository;
import com.stoury.utils.cachekeys.PopularSpotsKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static com.stoury.utils.cachekeys.HotFeedsKeys.getHotFeedsKey;

@Service
@RequiredArgsConstructor
public class RankingService {
    private final FeedRepository feedRepository;
    private final RankingRepository rankingRepository;
    private final LikeRepository likeRepository;

    public List<FeedResponse> getHotFeeds(ChronoUnit chronoUnit) {
        return rankingRepository.getRankedFeedIds(getHotFeedsKey(chronoUnit))
                .stream()
                .map(Long::parseLong)
                .map(feedRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(feed -> FeedResponse.from(feed, likeRepository.getLikes(feed.getId().toString())))
                .toList();
    }


    public List<String> getPopularAbroadSpots() {
        return rankingRepository.getRankedLocations(PopularSpotsKey.POPULAR_ABROAD_SPOTS);
    }

    public List<String> getPopularDomesticSpots() {
        return rankingRepository.getRankedLocations(PopularSpotsKey.POPULAR_DOMESTIC_SPOTS);
    }
}
