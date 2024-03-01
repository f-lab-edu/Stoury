package com.stoury.dto.diary;

import com.stoury.domain.Diary;
import com.stoury.dto.feed.FeedResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public record DiaryResponse(Long id, Long memberId, String title, String thumbnailPath, Map<Long, List<FeedResponse>> feeds,
                            LocalDate startDate, LocalDate endDate,
                            String city, String country, long likes, LocalDateTime createdAt) {

    public static DiaryResponse from(Diary diary,  List<FeedResponse> feeds) {
        List<FeedResponse> sortedFeeds = feeds.stream().sorted(Comparator.comparing(FeedResponse::createdAt)).toList();
        Map<Long, List<FeedResponse>> dailyFeeds = getDailyFeeds(sortedFeeds);

        FeedResponse firstFeed = sortedFeeds.get(0);

        FeedResponse lastFeed = sortedFeeds.get(sortedFeeds.size() - 1);

        long likesSum = dailyFeeds.values().stream()
                .flatMap(Collection::stream)
                .map(FeedResponse::likes)
                .mapToLong(Long::longValue)
                .sum();

        return new DiaryResponse(
                diary.getId(),
                diary.getMember().getId(),
                diary.getTitle(),
                diary.getThumbnail().getPath(),
                dailyFeeds,
                firstFeed.createdAt().toLocalDate(),
                lastFeed.createdAt().toLocalDate(),
                firstFeed.location().city(),
                firstFeed.location().country(),
                likesSum,
                diary.getCreatedAt()
        );
    }

    private static Map<Long, List<FeedResponse>> getDailyFeeds(List<FeedResponse> sortedFeeds) {
        LocalDate startDate = sortedFeeds.get(0).createdAt().toLocalDate();

        Map<Long, List<FeedResponse>> dailyFeeds = new TreeMap<>();

        for (FeedResponse feed : sortedFeeds) {
            long day = ChronoUnit.DAYS.between(startDate, feed.createdAt().toLocalDate()) + 1;

            dailyFeeds.computeIfAbsent(day, d -> new ArrayList<>()).add(feed);
        }

        return dailyFeeds;
    }
}
