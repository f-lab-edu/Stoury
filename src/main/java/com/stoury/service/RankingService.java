package com.stoury.service;

import com.stoury.domain.Feed;
import com.stoury.dto.feed.FeedResponse;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.LikeRepository;
import com.stoury.repository.RankingRepository;
import com.stoury.utils.CacheKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RankingService {
    private final FeedRepository feedRepository;
    private final RankingRepository rankingRepository;
    private final LikeRepository likeRepository;
    @Scheduled(fixedRate = 12 * 60 * 60 * 1000)
    public void updatePopularSpotsCache() {
        Pageable pageable = PageRequest.of(0, 10);
        List<String> rankedDomesticCities = feedRepository.findTop10CitiesInKorea(pageable);
        rankingRepository.update(CacheKeys.POPULAR_DOMESTIC_SPOTS, rankedDomesticCities);

        List<String> rankedCountries = feedRepository.findTop10CountriesNotKorea(pageable);
        rankingRepository.update(CacheKeys.POPULAR_ABROAD_SPOTS, rankedCountries);
    }

    public void updateDailyHotFeeds() {
        updateHotFeeds(ChronoUnit.DAYS);
    }

    public void updateWeeklyHotFeeds() {
        updateHotFeeds(ChronoUnit.WEEKS);
    }

    public void updateMonthlyHotFeeds() {
        updateHotFeeds(ChronoUnit.MONTHS);
    }

    private void updateHotFeeds(ChronoUnit chronoUnit) {
        Pageable pageable = PageRequest.of(0, 100);

        Page<Feed> page;
        while ((page = feedRepository.findAll(pageable)).hasNext()) {
            for (Feed feed : page.getContent()) {
                updateHotFeed(chronoUnit, feed);
            }

            if (page.hasNext()) {
                pageable = page.nextPageable();
            }
        }
    }

    private void updateHotFeed(ChronoUnit chronoUnit, Feed feed) {
        if (!likeRepository.existsByFeed(feed)) {
            return;
        }
        long currentLikes = likeRepository.getCountByFeed(feed);
        long prevLikes = likeRepository.getCountSnapshotByFeed(feed, chronoUnit);

        long likeIncrease = currentLikes - prevLikes;
        if (likeIncrease > 0) {
            rankingRepository.saveHotFeed(feed.getId().toString(), likeIncrease, chronoUnit);
        }
    }

    public List<FeedResponse> getHotFeeds(ChronoUnit chronoUnit) {
        return rankingRepository.getRankedList(CacheKeys.getHotFeedsKey(chronoUnit))
                .stream()
                .map(Long::parseLong)
                .map(feedRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(feed -> FeedResponse.from(feed, likeRepository.getLikes(feed.getId().toString())))
                .toList();
    }
}
