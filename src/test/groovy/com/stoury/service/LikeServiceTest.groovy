package com.stoury.service

import com.stoury.domain.Feed
import com.stoury.domain.Like
import com.stoury.domain.Member
import com.stoury.exception.AlreadyLikedFeedException
import com.stoury.exception.feed.FeedSearchException
import com.stoury.exception.member.MemberSearchException
import com.stoury.repository.FeedRepository
import com.stoury.repository.LikeRedisRepository
import com.stoury.repository.LikeRepository
import com.stoury.repository.MemberRepository
import com.stoury.service.LikeService
import spock.lang.Specification

class LikeServiceTest extends Specification {
    def likeDbRepository = Mock(LikeRepository)
    def likeRedisRepository = Mock(LikeRedisRepository)
    def memberRepository = Mock(MemberRepository)
    def feedRepository = Mock(FeedRepository)
    def likeService = new LikeService(likeDbRepository, likeRedisRepository, memberRepository, feedRepository)

    def liker = Mock(Member)
    def feed = Mock(Feed)

    def setup() {
        memberRepository.findById(_ as Long) >> Optional.of(liker)
        feedRepository.findById(_ as Long) >> Optional.of(feed)
    }

    def "좋아요 성공"() {
        when:
        likeService.like(1L, 2L);

        then:
        1 * likeRedisRepository.save(_ as Like)
        0 * likeDbRepository.save(_)
    }

    def "좋아요 실패 - 존재하지 않는 사용자"() {
        when:
        likeService.like(1L, 2L);

        then:
        memberRepository.findById(_ as Long) >> Optional.empty()
        thrown(MemberSearchException.class)
    }

    def "좋아요 실패 - 존재하지 않는 피드"() {
        when:
        likeService.like(1L, 2L);

        then:
        feedRepository.findById(_ as Long) >> Optional.empty()
        thrown(FeedSearchException.class)
    }

    def "좋아요 실패 - 이미 좋아요 한 피드 - db에서 발견"() {
        setup:
        likeDbRepository.existsByMemberAndFeed(_ as Member, _ as Feed) >> true

        when:
        likeService.like(1L, 2L);

        then:
        thrown(AlreadyLikedFeedException.class)
    }

    def "좋아요 실패 - 이미 좋아요 한 피드 - 캐시에서 발견"() {
        setup:
        likeRedisRepository.existsByMemberAndFeed(_ as Member, _ as Feed) >> true

        when:
        likeService.like(1L, 2L);

        then:
        thrown(AlreadyLikedFeedException.class)
    }

    def "좋아요 취소 성공 - 캐시에서 삭제"() {
        when:
        likeService.likeCancel(1L, 2L)
        then:
        1 * likeRedisRepository.deleteByMemberAndFeed(_, _) >> true
        0 * likeDbRepository.deleteByMemberAndFeed(_, _)
    }

    def "좋아요 취소 성공 - DB에서 삭제"() {
        when:
        likeService.likeCancel(1L, 2L)

        then:
        1 * likeRedisRepository.deleteByMemberAndFeed(_, _)
        1 * likeDbRepository.deleteByMemberAndFeed(_, _)
    }

    def "특정 피드의 좋아요만 가져오기"() {
        given:
        likeRedisRepository.countByFeed(_ as Feed) >> 2
        likeDbRepository.countByFeed(_ as Feed) >> 3
        when:
        def likes = likeService.getLikesOfFeed(1L)
        then:
        likes == 5
    }
}
