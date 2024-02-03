package com.stoury.repository;

import com.stoury.utils.CacheKeys;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RankingRepository {
    private final StringRedisTemplate redisTemplate;
    private ListOperations<String, String> opsForList;

    public RankingRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        opsForList = redisTemplate.opsForList();
    }

    public List<String> getRankedList(CacheKeys cacheKey) {
        return opsForList.range(cacheKey.name(), 0, -1);
    }

    public void update(CacheKeys cacheKey, List<String> rankedSpots) {
        redisTemplate.delete(cacheKey.name());
        opsForList.leftPushAll(cacheKey.name(), rankedSpots);
    }
}
