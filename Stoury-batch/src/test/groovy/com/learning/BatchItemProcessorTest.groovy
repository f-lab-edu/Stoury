package com.learning

import com.stoury.batch.BatchFollowersRecommendFeedsConfig
import com.stoury.batch.LogJobExecutionListener
import com.stoury.domain.Follow
import com.stoury.domain.Member
import com.stoury.dto.MemberRecommendFeedIds
import com.stoury.repository.FeedRepository
import com.stoury.repository.FollowRepository
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.item.Chunk
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import spock.lang.Specification

class BatchItemProcessorTest extends Specification {
    def logger = Mock(LogJobExecutionListener)
    def emf = Mock(EntityManagerFactory)
    def jobRepository = Mock(JobRepository)
    def transactionManager = Mock(PlatformTransactionManager)
    def taskExecutor = Mock(ThreadPoolTaskExecutor)
    def feedRepository = Mock(FeedRepository)
    def followRepository = Mock(FollowRepository)
    def batchConfig = new BatchFollowersRecommendFeedsConfig(logger, emf, jobRepository, transactionManager,
            taskExecutor, feedRepository, followRepository)

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
        def feedProcessor = batchConfig.followerViewedFeedsAggregator()
        when:
        def output = feedProcessor.process(1L)
        then:
        output.memberId() == 1L
        1 * feedRepository.findFollowerViewedFeedsOfMember(_) >> [1L,2L,3L]
    }
}
