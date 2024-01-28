package com.stoury

import com.stoury.domain.Feed
import com.stoury.domain.Like
import com.stoury.domain.Member
import com.stoury.exception.AlreadyLikedFeedException
import com.stoury.exception.feed.FeedSearchException
import com.stoury.exception.member.MemberSearchException
import com.stoury.repository.FeedRepository
import com.stoury.repository.LikeRepository
import com.stoury.repository.MemberRepository
import com.stoury.service.LikeService
import spock.lang.Specification

class LikeServiceTest extends Specification {
    def likeRepository = Mock(LikeRepository)
    def memberRepository = Mock(MemberRepository)
    def feedRepository = Mock(FeedRepository)
    def likeService = new LikeService(likeRepository, memberRepository, feedRepository)
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
        1 * likeRepository.save(_ as Like)
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

    def "좋아요 실패 - 이미 좋아요 한 피드"() {
        setup:
        likeRepository.existsByMemberAndFeed(_ as Member, _ as Feed) >> true

        when:
        likeService.like(1L, 2L);

        then:
        thrown(AlreadyLikedFeedException.class)
    }

    def "좋아요 취소 성공"() {
        when:
        likeService.likeCancel(1L, 2L)

        then:
        1 * likeRepository.deleteByMemberAndFeed(liker, feed)
    }

    def "특정 피드의 좋아요만 가져오기"() {
        when:
        likeService.getLikesOfFeed(1L)
        then:
        1 * likeRepository.countByFeed(_ as Feed)
    }
}
