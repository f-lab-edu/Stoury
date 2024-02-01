package com.stoury.repository;

import com.stoury.domain.Feed;
import com.stoury.domain.Like;
import com.stoury.domain.Member;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class LikeRepository {
    private final StringRedisTemplate redisTemplate;
    private SetOperations<String, String> opsForSet;

    public LikeRepository(StringRedisTemplate redisTemplate) {
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

    public void deleteByMemberAndFeed(Member liker, Feed feed) {
        String memberId = getMemberIdToString(liker);
        String feedId = getFeedIdToString(feed);

        opsForSet.remove(feedId, memberId);
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
}
