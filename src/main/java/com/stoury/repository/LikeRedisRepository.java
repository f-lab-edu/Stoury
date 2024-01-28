package com.stoury.repository;

import com.stoury.domain.Feed;
import com.stoury.domain.Like;
import com.stoury.domain.Member;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class LikeRedisRepository {
    private final StringRedisTemplate redisTemplate;
    private SetOperations<String, String> opsForSet;

    @PostConstruct
    private void init() {
        opsForSet = redisTemplate.opsForSet();
    }

    public Like save(Like like) {
        String feedId = Objects.requireNonNull(like.getFeed().getId(), "Feed id cannot be null").toString();
        String memberId = Objects.requireNonNull(like.getMember().getId(), "Member id cannot be null").toString();
        opsForSet.add(feedId, memberId);
        return like;
    }

    public boolean existsByMemberAndFeed(Member liker, Feed feed) {
        String memberId = Objects.requireNonNull(liker.getId(), "Member id cannot be null").toString();
        String feedId = Objects.requireNonNull(feed.getId(), "Feed id cannot be null").toString();

        return opsForSet.isMember(feedId, memberId);
    }

    public boolean deleteByMemberAndFeed(Member liker, Feed feed) {
        if (existsByMemberAndFeed(liker, feed)) {
            opsForSet.remove(feed.getId().toString(), liker.getId().toString());
            return true;
        }
        return false;
    }

    public int countByFeed(Feed feed) {
        String feedId = Objects.requireNonNull(feed.getId(), "Feed id cannot be null").toString();
        return Math.toIntExact(opsForSet.size(feedId));
    }

    private void clearAllOnlyForTest() {
        Set<String> allKeys = redisTemplate.keys("*");
        redisTemplate.delete(allKeys);
    }
}
