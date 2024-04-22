package com.stoury.service

import com.stoury.domain.Follow
import com.stoury.domain.Member
import com.stoury.repository.FollowRepository
import com.stoury.repository.MemberRepository
import spock.lang.Specification

class FollowServiceTest extends Specification {
    def memberRepository = Mock(MemberRepository)
    def followRepository = Mock(FollowRepository)
    def followService = new FollowService(memberRepository, followRepository)

    def "팔로우 기능"() {
        given:
        def followerId = 1L
        def followeeEmail = "test@test.com"
        def follower = Mock(Member)
        def followee = Mock(Member)
        memberRepository.findById(followerId) >> Optional.of(follower)
        memberRepository.findByEmail(followeeEmail) >> Optional.of(followee)
        when:
        followService.follow(followerId, followeeEmail)
        then:
        1 * followRepository.save(_ as Follow)
    }
}
