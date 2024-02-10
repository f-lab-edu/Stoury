package com.stoury.controller;

import com.stoury.dto.feed.SimpleFeedResponse;
import com.stoury.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RankingController {
    private final RankingService rankingService;

    @GetMapping("/rank/abroad-spots")
    public List<String> getPopularAbroadSpots() {
        return rankingService.getPopularAbroadSpots();
    }

    @GetMapping("/rank/domestic-spots")
    public List<String> getPopularDomesticSpots() {
        return rankingService.getPopularDomesticSpots();
    }

    @GetMapping("/rank/daily-hot-feeds")
    public List<SimpleFeedResponse> getDailyHotFeeds() {
        return rankingService.getHotFeeds(ChronoUnit.DAYS);
    }

    @GetMapping("/rank/weekly-hot-feeds")
    public List<SimpleFeedResponse> getWeeklyHotFeeds() {
        return rankingService.getHotFeeds(ChronoUnit.WEEKS);
    }

    @GetMapping("/rank/monthly-hot-feeds")
    public List<SimpleFeedResponse> getMonthlyHotFeeds() {
        return rankingService.getHotFeeds(ChronoUnit.MONTHS);
    }
}
