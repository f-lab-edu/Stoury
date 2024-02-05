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
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    public void updatePopularLocations() {
        Pageable pageable = PageRequest.of(0, 10);
        List<String> rankedDomesticCities = feedRepository.findTop10CitiesInKorea(pageable);
        rankingRepository.update(CacheKeys.POPULAR_DOMESTIC_SPOTS, rankedDomesticCities);

        List<String> rankedCountries = feedRepository.findTop10CountriesNotKorea(pageable);
        rankingRepository.update(CacheKeys.POPULAR_ABROAD_SPOTS, rankedCountries);
    }

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    public void updateDailyHotFeeds() {
        updateHotFeeds(ChronoUnit.DAYS);
    }

    @Scheduled(cron = "0 0 0 ? * SAT") // 매주 토요일 자정에 실행
    public void updateWeeklyHotFeeds() {
        updateHotFeeds(ChronoUnit.WEEKS);
    }

    @Scheduled(cron = "0 0 0 L * ?") // 매월 마지막 날 자정에 실행
    public void updateMonthlyHotFeeds() {
        updateHotFeeds(ChronoUnit.MONTHS);
    }

    private void updateHotFeeds(ChronoUnit chronoUnit) {
        Pageable pageable = PageRequest.of(0, 100);

        Page<Feed> page;
        while ((page = feedRepository.findAll(pageable)).hasNext()) {
            for (Feed feed : page.getContent()) {
                updateHotFeed(chronoUnit, feed.getId().toString());
            }

            if (page.hasNext()) {
                pageable = page.nextPageable();
            }
        }
    }

    private void updateHotFeed(ChronoUnit chronoUnit, String feedId) {
        if (!likeRepository.existsByFeedId(feedId)) {
            return;
        }
        long currentLikes = likeRepository.getCountByFeedId(feedId);
        long prevLikes = likeRepository.getCountSnapshotByFeed(feedId, chronoUnit);

        long likeIncrease = currentLikes - prevLikes;
        if (likeIncrease > 0) {
            rankingRepository.saveHotFeed(feedId, likeIncrease, chronoUnit);
        }
    }

    public List<FeedResponse> getHotFeeds(ChronoUnit chronoUnit) {
        return rankingRepository.getRankedFeedIds(CacheKeys.getHotFeedsKey(chronoUnit))
                .stream()
                .map(Long::parseLong)
                .map(feedRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(feed -> FeedResponse.from(feed, likeRepository.getLikes(feed.getId().toString())))
                .toList();
    }
}
