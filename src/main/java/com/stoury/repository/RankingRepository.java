package com.stoury.repository;

import com.stoury.utils.CacheKeys;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.stoury.utils.CacheKeys.getHotFeedsKey;

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

    public List<String> getRankedList(CacheKeys cacheKey) {
        return opsForList.range(cacheKey.name(), 0, -1);
    }

    public void update(CacheKeys cacheKey, List<String> rankedSpots) {
        redisTemplate.delete(cacheKey.name());
        opsForList.leftPushAll(cacheKey.name(), rankedSpots);
    }

    public void saveHotFeed(String feedId, double likeIncrease, ChronoUnit chronoUnit) {
        String key = String.valueOf(getHotFeedsKey(chronoUnit));
        opsForZset.add(key, feedId, likeIncrease);
    }
}
