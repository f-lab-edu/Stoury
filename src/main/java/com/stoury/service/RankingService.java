package com.stoury.service;

import com.stoury.dto.feed.FeedResponse;
import com.stoury.exception.feed.FeedSearchException;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.LikeRepository;
import com.stoury.repository.RankingRepository;
import com.stoury.utils.cachekeys.HotFeedsKeys;
import com.stoury.utils.cachekeys.PopularSpotsKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import static com.stoury.utils.cachekeys.HotFeedsKeys.getHotFeedsKey;

@Service
@RequiredArgsConstructor
public class RankingService {
    private final FeedRepository feedRepository;
    private final RankingRepository rankingRepository;
    private final LikeRepository likeRepository;

    public List<FeedResponse> getHotFeeds(ChronoUnit chronoUnit) {
        List<Long> feedIds = rankingRepository.getRankedFeedIds(getHotFeedsKey(chronoUnit))
                .stream()
                .map(Long::parseLong)
                .toList();
        List<FeedResponse> hotFeeds =  feedRepository.findAllById(feedIds)
                .stream()
                .map(feed -> FeedResponse.from(feed, likeRepository.getLikes(feed.getId().toString())))
                .sorted(Comparator.comparing(FeedResponse::likes).reversed())
                .toList();
        if (hotFeeds.size() != feedIds.size()) {
            throw new FeedSearchException("Cannot find HotFeeds");
        }

        return hotFeeds;
    }


    public List<String> getPopularAbroadSpots() {
        return rankingRepository.getRankedLocations(PopularSpotsKey.POPULAR_ABROAD_SPOTS);
    }

    public List<String> getPopularDomesticSpots() {
        return rankingRepository.getRankedLocations(PopularSpotsKey.POPULAR_DOMESTIC_SPOTS);
    }

    public boolean isRankedFeed(Long feedId) {
        for (HotFeedsKeys key : HotFeedsKeys.values()) {
            if (rankingRepository.contains(key, feedId.toString())) {
                return true;
            }
        }
        return false;
    }
}
