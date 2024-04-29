package com.learning

import com.stoury.batch.BatchFollowersRecommendFeedsConfig
import com.stoury.batch.LogJobExecutionListener
import com.stoury.domain.Follow
import com.stoury.domain.Member
import com.stoury.dto.MemberRecommendFeedIds
import com.stoury.repository.FeedRepository
import com.stoury.repository.FollowRepository
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.item.Chunk
import spock.lang.Specification

class BatchItemProcessorTest extends Specification {
    def logger = Mock(LogJobExecutionListener)
    def emf = Mock(EntityManagerFactory)
    def feedRepository = Mock(FeedRepository)
    def followRepository = Mock(FollowRepository)
    def batchConfig = new BatchFollowersRecommendFeedsConfig(logger, emf, feedRepository, followRepository)

    def "recommendFeedsWriter 테스트"() {
        given:
        def feedWriter = batchConfig.recommendFeedsWriter()
        def recommendFeedsChunk = Chunk.of(
                new MemberRecommendFeedIds(1L, [2L,3L,4L] as Set),
                new MemberRecommendFeedIds(2L, [2L,3L,4L,5L] as Set),
                new MemberRecommendFeedIds(3L, [4L, 7L] as Set),
                new MemberRecommendFeedIds(4L, [3L] as Set)
        )
        when:
        feedWriter.write(recommendFeedsChunk)
        then:
        1 * feedRepository.saveRecommendFeeds(_)
    }

    def "followViewedFeedsProcessor 테스트"() {
        given:
        def feedProcessor = batchConfig.followViewedFeedsProcessor()
        def follower2 = new Member(id: 2L)
        def follower3 = new Member(id: 3L)
        def follower4 = new Member(id: 4L)
        def member = new Member(id:1L, followers: [
                new Follow(Mock(Member), follower2),
                new Follow(Mock(Member), follower3),
                new Follow(Mock(Member), follower4),
        ] as Set)
        followRepository.findFollowerId(1L) >> [follower2, follower3, follower4]
        when:
        def output = feedProcessor.process(member.id)
        then:
        output.memberId() == 1L
        3 * feedRepository.findViewedFeedIdsByMember(_) >> []
    }
}
