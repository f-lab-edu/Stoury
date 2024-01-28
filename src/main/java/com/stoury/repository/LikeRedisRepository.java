package com.stoury.repository;

import com.stoury.domain.Feed;
import com.stoury.domain.Like;
import com.stoury.domain.Member;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

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
        // like 저장

        return null;
    }

    public boolean existsByMemberAndFeed(Member liker, Feed feed) {
        // feed의 liker가 있는지 확인

        return false;
    }

    public boolean deleteByMemberAndFeed(Member liker, Feed feed) {
        if (existsByMemberAndFeed(liker, feed)) {
            // feed에서 liker 삭제
            return true;
        }
        return false;
    }

    public int countByFeed(Feed feed) {
        // feed의 liker 수 계산

        return 0;
    }
}
