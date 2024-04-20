package com.stoury.repository;

import com.stoury.utils.cachekeys.PageSize;
import com.stoury.utils.cachekeys.RecommendFeedsKey;
import com.stoury.utils.cachekeys.ViewedFeedsKey;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Repository
public class RecommendFeedsRepository {
    private final SetOperations<String, String> opsForSet;

    public RecommendFeedsRepository(StringRedisTemplate redisTemplate) {
        this.opsForSet = redisTemplate.opsForSet();
    }


    public List<Long> findAllByMemberId(Long memberId) {
        String memberIdNonNull = Objects.requireNonNull(memberId, "member id cannot be null").toString();

        String key = RecommendFeedsKey.getRecommendFeedsKey(memberIdNonNull);

        List<String> randomMembers = opsForSet.randomMembers(key, PageSize.RECOMMEND_FEEDS_SIZE);
        if (randomMembers == null) {
            return Collections.emptyList();
        }

        return randomMembers.stream()
                .map(Long::parseLong)
                .toList();
    }

    public void addViewedFeed(Long memberId, Long feedId) {
        String memberIdNonNull = Objects.requireNonNull(memberId, "member id cannot be null").toString();
        String feedIdNonNull = Objects.requireNonNull(feedId, "feed id cannot be null").toString();

        String key = ViewedFeedsKey.getViewedFeedsKey(memberIdNonNull);

        opsForSet.add(key, feedIdNonNull);
    }

    public List<Long> getViewedFeed(Long memberId){
        String memberIdNonNull = Objects.requireNonNull(memberId, "member id cannot be null").toString();
        String key = ViewedFeedsKey.getViewedFeedsKey(memberIdNonNull);

        Set<String> viewedFeeds = opsForSet.members(key);
        if (viewedFeeds == null) {
            return Collections.emptyList();
        }

        return viewedFeeds.stream().map(Long::parseLong).toList();
    }

}
