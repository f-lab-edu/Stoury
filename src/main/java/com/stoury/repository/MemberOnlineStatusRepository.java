package com.stoury.repository;

import com.stoury.exception.location.GetMemberPositionsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class MemberOnlineStatusRepository {
    public static final String ONLINE_MEMBER_CACHE_KEY = "online:member:";
    public static final String MEMBER_POS_CACHE_KEY = "member:pos:";
    private final SetOperations<String, String> opsForSet;
    private final GeoOperations<String, String> opsForGeo;

    @Autowired
    public MemberOnlineStatusRepository(StringRedisTemplate redisTemplate) {
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

    public Map<Long, Integer> findByPoint(Point point, double radiusKm) {
        Circle within = new Circle(point, new Distance(radiusKm, Metrics.KILOMETERS));
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .sortAscending();
        long start = System.currentTimeMillis();
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> geoResults = Optional.ofNullable(opsForGeo.radius(MEMBER_POS_CACHE_KEY, within, args))
                .orElseThrow(GetMemberPositionsException::new)
                .getContent();
        long end = System.currentTimeMillis();
        log.debug("geoRadius latency: {}", end - start);

        return geoResults.stream()
                .filter(this::nonNull)
                .map(res -> Map.entry(getId(res), getDistance(res)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private boolean nonNull(GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult){
        if(geoResult.getContent().getName() == null){
            log.error("Null member Id is stored. Check the online member redis storage.");
            return false;
        }
        return true;
    }

    private Long getId(GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult){
        String memberIdStr = geoResult.getContent().getName();
        return Long.valueOf(memberIdStr);
    }

    private Integer getDistance(GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult) {
        double distance = geoResult.getDistance().in(Metrics.KILOMETERS).getValue();
        return (int)distance;
    }
}
