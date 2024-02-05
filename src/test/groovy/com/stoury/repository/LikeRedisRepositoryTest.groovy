package com.stoury.repository

import com.stoury.domain.Feed
import com.stoury.domain.Like
import com.stoury.domain.Member
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import spock.lang.Specification

class LikeRedisRepositoryTest extends Specification {
    def connectionFactory = new LettuceConnectionFactory("127.0.0.1", 8379) // 테스트용 레디스 컨테이너의 포트
    def likeRedisRepository = new LikeRepository(new StringRedisTemplate(connectionFactory))

    def setup() {
        connectionFactory.start()
    }

    def cleanup() {
        def redisTemplate = likeRedisRepository.redisTemplate
        Set<String> allKeys = redisTemplate.keys("*");
        redisTemplate.delete(allKeys);
    }

    def "좋아요 저장 성공"() {
        given:
        def member = new Member()
        def feed = new Feed()
        member.id = 1L
        feed.id = 2L
        def like = new Like(member, feed)

        when:
        likeRedisRepository.save(like)
        then:
        likeRedisRepository.existsByMemberAndFeed(member, feed)
    }

    def "피드의 좋아요 개수 가져오기"() {
        given:
        def member1 = new Member()
        def member2 = new Member()
        def member3 = new Member()
        def feed = new Feed()
        member1.id = 1L
        member2.id = 2L
        member3.id = 3L
        feed.id = 1L

        likeRedisRepository.save(new Like(member1, feed))
        likeRedisRepository.save(new Like(member2, feed))
        likeRedisRepository.save(new Like(member3, feed))

        when:
        def likes = likeRedisRepository.getCountByFeed(feed)
        then:
        likes == 3
    }

    def "좋아요 삭제 - 성공"() {
        given:
        def member = new Member()
        def feed = new Feed()
        member.id = 1L
        feed.id = 2L
        likeRedisRepository.save(new Like(member, feed))

        expect:
        likeRedisRepository.deleteByMemberAndFeed("1", "2")
        !likeRedisRepository.existsByMemberAndFeed(member, feed)
    }

    def "좋아요 삭제 - 실패, 없는 데이터 삭제 시도"() {
        expect:
        !likeRedisRepository.deleteByMemberAndFeed("1", "2")
    }
}
