package com.stoury.repository;

import com.stoury.domain.Feed;
import com.stoury.domain.Like;
import com.stoury.domain.Member;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;


import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.stoury.utils.CacheKeys.*;

@Repository
public class LikeRepository {
    private final StringRedisTemplate redisTemplate;
    private final SetOperations<String, String> opsForSet;
    private final ValueOperations<String, String> opsForVal;


    public LikeRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        opsForSet = redisTemplate.opsForSet();
        opsForVal = redisTemplate.opsForValue();
    }

    public Like save(Like like) {
        String feedId = getFeedIdToString(like.getFeed());
        String memberId = getMemberIdToString(like.getMember());
        opsForSet.add(getLikersKey(feedId), memberId);
        return like;
    }

    public boolean existsByMemberAndFeed(Member liker, Feed feed) {
        String memberId = getMemberIdToString(liker);
        String feedId = getFeedIdToString(feed);

        return Boolean.TRUE.equals(opsForSet.isMember(getLikersKey(feedId), memberId));
    }

    public void deleteByMemberAndFeed(Member liker, Feed feed) {
        String memberId = getMemberIdToString(liker);
        String feedId = getFeedIdToString(feed);

        opsForSet.remove(getLikersKey(feedId), memberId);
    }

    public long getCountByFeed(Feed feed) {
        String feedId = getFeedIdToString(feed);
        return getLikes(feedId);
    }

    public long getCountSnapshotByFeed(Feed feed, ChronoUnit chronoUnit) {
        String countSnapshotKey = getCountSnapshotKey(chronoUnit, feed.getId().toString());
        String countStr = opsForVal.get(countSnapshotKey);
        return Optional.ofNullable(countStr).map(Long::parseLong).orElse(0L);
    }

    public Long getLikes(String feedId) {
        return opsForSet.size(getLikersKey(feedId));
    }

    private String getFeedIdToString(Feed feed) {
        Long feedId = Objects.requireNonNull(feed.getId(), "Feed id cannot be null");

        return feedId.toString();
    }

    private String getMemberIdToString(Member member) {
        Long memberId = Objects.requireNonNull(member.getId(), "Member id cannot be null");

        return memberId.toString();
    }


    public boolean existsByFeed(Feed feed) {
        String likersKey = getLikersKey(feed.getId().toString());
        return Boolean.TRUE.equals(redisTemplate.hasKey(likersKey));
    }
}
