package com.stoury.repository;

import com.stoury.domain.Feed;
import com.stoury.domain.Like;
import com.stoury.domain.Member;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Set;

@Repository
public class LikeRedisRepository {
    private final StringRedisTemplate redisTemplate;
    private SetOperations<String, String> opsForSet;

    public LikeRedisRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        opsForSet = redisTemplate.opsForSet();
    }

    public Like save(Like like) {
        String feedId = getFeedIdToString(like.getFeed());
        String memberId = getMemberIdToString(like.getMember());
        opsForSet.add(feedId, memberId);
        return like;
    }

    public boolean existsByMemberAndFeed(Member liker, Feed feed) {
        String memberId = getMemberIdToString(liker);
        String feedId = getFeedIdToString(feed);

        return opsForSet.isMember(feedId, memberId);
    }

    public boolean deleteByMemberAndFeed(Member liker, Feed feed) {
        if (existsByMemberAndFeed(liker, feed)) {
            String memberId = getMemberIdToString(liker);
            String feedId = getFeedIdToString(feed);

            opsForSet.remove(feedId, memberId);
            return true;
        }
        return false;
    }

    public long countByFeed(Feed feed) {
        String feedId = getFeedIdToString(feed);
        return opsForSet.size(feedId);
    }

    private String getFeedIdToString(Feed feed) {
        Long feedId = Objects.requireNonNull(feed.getId(), "Feed id cannot be null");

        return feedId.toString();
    }

    private String getMemberIdToString(Member member) {
        Long memberId = Objects.requireNonNull(member.getId(), "Member id cannot be null");

        return memberId.toString();
    }

    private void clearAllOnlyForTest() {
        Set<String> allKeys = redisTemplate.keys("*");
        redisTemplate.delete(allKeys);
    }
}
