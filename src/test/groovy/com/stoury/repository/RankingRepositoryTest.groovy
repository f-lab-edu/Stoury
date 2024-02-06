package com.stoury.repository

import com.stoury.utils.CacheKeys
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import spock.lang.Specification

import java.time.temporal.ChronoUnit
import java.util.stream.IntStream

class RankingRepositoryTest extends Specification {
    def connectionFactory = new LettuceConnectionFactory("127.0.0.1", 8379) // 테스트용 레디스 컨테이너의 포트
    def rankingRepository = new RankingRepository(new StringRedisTemplate(connectionFactory))

    def setup() {
        connectionFactory.start()
    }

    def cleanup() {
        def redisTemplate = rankingRepository.redisTemplate
        Set<String> allKeys = redisTemplate.keys("*");
        redisTemplate.delete(allKeys);
    }

    def "인기 피드 랭킹"() {
        given:
        def likeIncreases = [
              //0    1   2   3    4
                5,   13, 1,  100, 2,
              //5    6   7   8    9
                111, 32, 23, 8,   3,
              //10   11  12  13   14
                10,  24, 98, 55,  101,
              //15   16  17  18   19
                333, 31, 56, 74,  6
        ]
        when:
        IntStream.range(0, 20)
                .forEach(i -> rankingRepository.saveHotFeed(String.valueOf(i), likeIncreases.get(i), ChronoUnit.DAYS))

        then:
        def rankedList = rankingRepository.getRankedFeedIds(CacheKeys.getHotFeedsKey(ChronoUnit.DAYS))
        def expectedList = List.of(
                15, 5,  14, 3,  12,
                       18, 17, 13, 6,  16,
                       11, 7,  1,  10, 8,
                       19, 0,  9,  4,  2)
        (0..<20).each { i ->
            rankedList.get(i) == expectedList.get(i)
        }
    }
}
