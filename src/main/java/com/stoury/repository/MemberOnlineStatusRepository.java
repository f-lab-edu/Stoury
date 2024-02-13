package com.stoury.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    public void save(Long memberId, Double latitude, Double longitude) {
        opsForSet.add(ONLINE_MEMBER_CACHE_KEY, memberId.toString());

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

    public List<Pair<String, Integer>> findByPoint(Point point, double radiusKm) {
        Circle within = new Circle(point, new Distance(radiusKm, Metrics.KILOMETERS));
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .sortAscending();
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> geoResults = opsForGeo.radius(MEMBER_POS_CACHE_KEY, within, args)
                .getContent();

        return geoResults.stream()
                .map(MemberOnlineStatusRepository::memberDistances)
                .toList();
    }

    public static Pair<String, Integer> memberDistances(GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult) {
        String memberId = geoResult.getContent().getName();
        double distance = geoResult.getDistance().in(Metrics.KILOMETERS).getNormalizedValue();

        return Pair.of(memberId, (int) distance);
    }
}
