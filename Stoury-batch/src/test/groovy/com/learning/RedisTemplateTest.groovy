package com.learning

import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import spock.lang.Specification

class RedisTemplateTest extends Specification {
    def connectionFactory = new LettuceConnectionFactory("localhost", 8379)
    def redisTemplate = new StringRedisTemplate(connectionFactory)
    def testKey = "TestKey"

    def setup(){
        connectionFactory.start()
        redisTemplate.delete(testKey)
    }

    def "opsForSet에 addAll() 가능한지"(){
        given:
        def opsForSet = redisTemplate.opsForSet()
        def list = [1L, 2L, 3L, 4L]
        String[] stringArr = list.stream().map(String::valueOf).toArray()
        when:
        opsForSet.add(testKey, stringArr)
        then:
        opsForSet.members(testKey).size() == 4
    }
}
