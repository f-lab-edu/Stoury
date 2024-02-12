package com.stoury.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stoury.dto.member.OnlineMember;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class MemberOnlineStatusRepository {
    public static final String ONLINE_MEMBER_CACHE_KEY_PREFIX = "online:member:";
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public MemberOnlineStatusRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void save(OnlineMember onlineMember) {
        Long memberId = onlineMember.memberId();
        String rawOnlineMemberJson;
        try {
            rawOnlineMemberJson = objectMapper.writeValueAsString(onlineMember);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot convert onlineMember to json", e);
        }
        redisTemplate.opsForValue().set(ONLINE_MEMBER_CACHE_KEY_PREFIX + memberId, rawOnlineMemberJson);
    }

    public void delete(Long memberId) {
        String memberIdNotNull = Objects.requireNonNull(memberId, "Member id cannot be null").toString();
        redisTemplate.delete(ONLINE_MEMBER_CACHE_KEY_PREFIX + memberIdNotNull);
    }
}
