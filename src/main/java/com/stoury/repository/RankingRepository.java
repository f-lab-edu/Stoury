package com.stoury.repository;

import com.stoury.utils.cachekeys.HotFeedsKeys;
import com.stoury.utils.cachekeys.PopularSpotsKey;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.stoury.utils.cachekeys.HotFeedsKeys.getHotFeedsKey;

@Repository
public class RankingRepository {
    private final StringRedisTemplate redisTemplate;
    private final ListOperations<String, String> opsForList;
    private final ZSetOperations<String, String> opsForZset;

    public RankingRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        opsForList = redisTemplate.opsForList();
        opsForZset = redisTemplate.opsForZSet();
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

    public void saveHotFeed(String feedId, double likeIncrease, ChronoUnit chronoUnit) {
        String key = String.valueOf(getHotFeedsKey(chronoUnit));
        opsForZset.add(key, feedId, likeIncrease);
    }

    public List<String> getRankedFeedIds(HotFeedsKeys key) {
        Set<String> rankedFeedIds = opsForZset.reverseRange(key.name(), 0, -1);
        if (rankedFeedIds == null) {
            return Collections.emptyList();
        }
        return rankedFeedIds.stream().toList();
    }

    public boolean contains(HotFeedsKeys key, String feedId) {
        Set<String> ids = opsForZset.range(key.toString(), 0, -1);

        return ids != null && ids.contains(feedId);
    }
}
