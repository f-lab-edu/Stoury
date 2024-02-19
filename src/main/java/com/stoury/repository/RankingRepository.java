package com.stoury.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stoury.dto.diary.SimpleDiaryResponse;
import com.stoury.dto.feed.SimpleFeedResponse;
import com.stoury.utils.cachekeys.HotFeedsKeys;
import com.stoury.utils.cachekeys.PopularSpotsKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.stoury.utils.cachekeys.HotFeedsKeys.getHotFeedsKey;

@Repository
public class RankingRepository {
    private final StringRedisTemplate redisTemplate;
    private final ListOperations<String, String> opsForList;
    private final ZSetOperations<String, String> opsForZset;
    private final ObjectMapper objectMapper;

    public RankingRepository(StringRedisTemplate redisTemplate, @Autowired ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        opsForList = redisTemplate.opsForList();
        opsForZset = redisTemplate.opsForZSet();
        this.objectMapper = objectMapper;
    }

    public List<String> getRankedLocations(PopularSpotsKey cacheKey) {
        return opsForList.range(cacheKey.name(), 0, -1);
    }

    public void update(PopularSpotsKey cacheKey, List<String> rankedSpots) {
        if (rankedSpots == null || rankedSpots.isEmpty()) {
            return;
        }
        redisTemplate.delete(cacheKey.name());
        opsForList.leftPushAll(cacheKey.name(), rankedSpots);
    }

    public void saveHotFeed(SimpleFeedResponse simpleFeed, double likeIncrease, ChronoUnit chronoUnit) {
        String key = String.valueOf(getHotFeedsKey(chronoUnit));
        String simpleFeedJson = getJsonString(simpleFeed);
        opsForZset.add(key, simpleFeedJson, likeIncrease);
    }

    public void saveHotDiaries(SimpleDiaryResponse simpleDiary, double likeIncrease) {
        String simpleDiaryJson = getJsonString(simpleDiary);
        opsForZset.add("HotDiaries", simpleDiaryJson, likeIncrease);
    }

    private String getJsonString(Object simpleFeed) {
        String simpleFeedJson = null;
        try {
            simpleFeedJson = objectMapper.writeValueAsString(simpleFeed);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return simpleFeedJson;
    }

    public List<SimpleFeedResponse> getRankedFeeds(HotFeedsKeys key) {
        Set<String> rankedSimpleFeeds = opsForZset.reverseRange(key.name(), 0, 9);
        if (rankedSimpleFeeds == null) {
            return Collections.emptyList();
        }
        return rankedSimpleFeeds.stream()
                .map(this::getFeedResponse)
                .filter(Objects::nonNull)
                .toList();
    }

    private SimpleFeedResponse getFeedResponse(String rawSimpleFeedJson) {
        try {
            return objectMapper.readValue(rawSimpleFeedJson, SimpleFeedResponse.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public boolean contains(HotFeedsKeys key, String feedId) {
        Set<String> ids = opsForZset.range(key.toString(), 0, -1);

        return ids != null && ids.contains(feedId);
    }

    public List<SimpleDiaryResponse> getRankedDiaries() {
        Set<String> rankedSimpleDiaries = opsForZset.reverseRange("HotDiaries", 0, 9);
        if (rankedSimpleDiaries == null) {
            return Collections.emptyList();
        }
        return rankedSimpleDiaries.stream()
                .map(this::getDiaryResponse)
                .filter(Objects::nonNull)
                .toList();
    }

    private SimpleDiaryResponse getDiaryResponse(String rawSimpleDiaryJson) {
        try {
            return objectMapper.readValue(rawSimpleDiaryJson, SimpleDiaryResponse.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
