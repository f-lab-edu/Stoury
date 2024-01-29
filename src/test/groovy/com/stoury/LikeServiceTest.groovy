package com.stoury

import com.stoury.domain.Feed
import com.stoury.domain.Like
import com.stoury.domain.Member
import com.stoury.exception.AlreadyLikedFeedException
import com.stoury.exception.feed.FeedSearchException
import com.stoury.exception.member.MemberSearchException
import com.stoury.repository.LikeRepository
import com.stoury.service.LikeService
import com.stoury.validator.Validator
import spock.lang.Specification

class LikeServiceTest extends Specification {
    def validator = Mock(Validator)
    def likeRepository = Mock(LikeRepository)
    def likeService = new LikeService(likeRepository, validator)
    def liker = Mock(Member)
    def feed = Mock(Feed)

    def "좋아요 성공"() {
        when:
        likeService.like(liker, feed);

        then:
        1 * likeRepository.save(_ as Like)
    }

    def "좋아요 실패 - 존재하지 않는 사용자"() {
        setup:
        validator.isMemberExists(_ as Member) >> {throw new MemberSearchException()}

        when:
        likeService.like(liker, feed);

        then:
        thrown(MemberSearchException.class)
    }

    def "좋아요 실패 - 존재하지 않는 피드"() {
        setup:
        validator.isFeedExists(_ as Feed) >> {throw new FeedSearchException()}

        when:
        likeService.like(liker, feed);

        then:
        thrown(FeedSearchException.class)
    }

    def "좋아요 실패 - 이미 좋아요 한 피드"() {
        setup:
        likeRepository.existsByMemberAndFeed(_ as Member, _ as Feed) >> true

        when:
        likeService.like(liker, feed);

        then:
        thrown(AlreadyLikedFeedException.class)
    }

    def "좋아요 취소 성공"() {
        when:
        likeService.likeCancel(liker, feed)

        then:
        1 * likeRepository.deleteByMemberAndFeed(liker, feed)
    }
}
