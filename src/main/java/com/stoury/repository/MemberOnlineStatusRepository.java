package com.stoury.repository;

import com.stoury.dto.member.OnlineMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class MemberOnlineStatusRepository {
    public static final String ONLINE_MEMBER_CACHE_KEY = "online:member:";
    public static final String MEMBER_POS_CACHE_KEY = "member:pos:";
    private final StringRedisTemplate redisTemplate;
    private final SetOperations<String, String> opsForSet;
    private final GeoOperations<String, String> opsForGeo;

    @Autowired
    public MemberOnlineStatusRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        opsForSet = redisTemplate.opsForSet();
        opsForGeo = redisTemplate.opsForGeo();
    }

    public void save(OnlineMember onlineMember) {
        Long memberId = onlineMember.memberId();
        opsForSet.add(ONLINE_MEMBER_CACHE_KEY, memberId.toString());
        Double latitude = onlineMember.latitude();
        Double longitude = onlineMember.longitude();

        if (Objects.nonNull(latitude) && Objects.nonNull(longitude)) {
            Point point = new Point(longitude, latitude);
            opsForGeo.add(MEMBER_POS_CACHE_KEY, point, memberId.toString());
        }
    }

    public void delete(Long memberId) {
        String memberIdNotNull = Objects.requireNonNull(memberId, "Member id cannot be null").toString();
        opsForSet.remove(ONLINE_MEMBER_CACHE_KEY, memberIdNotNull);
        opsForGeo.remove(MEMBER_POS_CACHE_KEY, memberId.toString());
    }
}
