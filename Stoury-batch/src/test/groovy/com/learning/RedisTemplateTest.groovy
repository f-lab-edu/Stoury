package com.learning

import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.Cursor
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.StringRedisTemplate
import spock.lang.Specification

class RedisTemplateTest extends Specification {
    def connectionFactory = new LettuceConnectionFactory("localhost", 8379)
    def redisTemplate = new StringRedisTemplate(connectionFactory)
    def testKey = "TestKey"
    Cursor<byte[]> cursor

    def setup(){
        connectionFactory.start()
        redisTemplate.delete(testKey)
    }

    def cleanup() {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close()
        }
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

    def "SCAN 했는데 왜 key가 반환이 안되지"() {
        given:
        redisTemplate.opsForSet().add("FrequentTags:3333", "val1")
        redisTemplate.opsForSet().add("FrequentTags:2222", "val1")
        redisTemplate.opsForSet().add("FrequentTags:1113", "val1")
        def scanOption = ScanOptions.scanOptions().match("FrequentTags:*").count(1).build()
        cursor = redisTemplate.getConnectionFactory().getConnection().keyCommands().scan(scanOption)
        expect:
        cursor.hasNext()
        cursor.hasNext()
        cursor.hasNext()
    }
}
