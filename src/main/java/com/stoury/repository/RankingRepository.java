package com.stoury.repository;

import com.stoury.dto.diary.SimpleDiaryResponse;
import com.stoury.dto.feed.SimpleFeedResponse;
import com.stoury.utils.JsonMapper;
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
    public static final String HOT_DIARIES_KEY = "HotDiaries";
    private final StringRedisTemplate redisTemplate;
    private final ListOperations<String, String> opsForList;
    private final ZSetOperations<String, String> opsForZset;
    private final JsonMapper jsonMapper;

    public RankingRepository(StringRedisTemplate redisTemplate, @Autowired JsonMapper jsonMapper) {
        this.redisTemplate = redisTemplate;
        opsForList = redisTemplate.opsForList();
        opsForZset = redisTemplate.opsForZSet();
        this.jsonMapper = jsonMapper;
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
        String simpleFeedJson = jsonMapper.getJsonString(simpleFeed);
        opsForZset.add(key, simpleFeedJson, likeIncrease);
    }

    public void saveHotDiaries(SimpleDiaryResponse simpleDiary, double likeIncrease) {
        String simpleDiaryJson = jsonMapper.getJsonString(simpleDiary);
        opsForZset.add(HOT_DIARIES_KEY, simpleDiaryJson, likeIncrease);
    }

    public List<SimpleFeedResponse> getRankedFeeds(HotFeedsKeys key) {
        Set<String> rankedSimpleFeeds = opsForZset.reverseRange(key.name(), 0, 9);
        if (rankedSimpleFeeds == null) {
            return Collections.emptyList();
        }
        return rankedSimpleFeeds.stream()
                .map(jsonMapper::getFeedResponse)
                .filter(Objects::nonNull)
                .toList();
    }

    public boolean contains(HotFeedsKeys key, String feedId) {
        Set<String> ids = opsForZset.range(key.toString(), 0, -1);

        return ids != null && ids.contains(feedId);
    }

    public List<SimpleDiaryResponse> getRankedDiaries() {
        Set<String> rankedSimpleDiaries = opsForZset.reverseRange(HOT_DIARIES_KEY, 0, 9);
        if (rankedSimpleDiaries == null) {
            return Collections.emptyList();
        }
        return rankedSimpleDiaries.stream()
                .map(jsonMapper::getDiaryResponse)
                .filter(Objects::nonNull)
                .toList();
    }
}
