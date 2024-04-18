package com.stoury

import com.fasterxml.jackson.databind.ObjectMapper
import com.stoury.batch.BatchRecommendFeedsConfig
import com.stoury.dto.FrequentTags
import com.stoury.projection.FeedResponseEntity
import com.stoury.repository.FeedRepository
import com.stoury.repository.RecommendFeedsRepository
import com.stoury.utils.JsonMapper
import com.stoury.utils.cachekeys.PageSize
import jakarta.persistence.EntityManagerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import spock.lang.Specification

class RecommendFeedsBatchMockTest extends Specification {
    def redisTemplate = Mock(StringRedisTemplate)
    def emf = Mock(EntityManagerFactory)
    def recommendFeedRepository = Mock(RecommendFeedsRepository)
    def feedRepository = Mock(FeedRepository)
    def jsonMapper = new JsonMapper(new ObjectMapper())
    def batchConfig = new BatchRecommendFeedsConfig(redisTemplate, emf, feedRepository, recommendFeedRepository, jsonMapper)

    def "viewedTagProcessor 테스트"() {
        given:
        feedRepository.findAllFeedsByIdIn(_) >> [
                new FeedResponseEntity(tagNames: '[ "tag1", "tag2", "tag3", "tag4" ]'),
                new FeedResponseEntity(tagNames: '[ "tag2", "tag3", "tag4", "tag5" ]'),
                new FeedResponseEntity(tagNames: '[ "tag3", "tag4"]'),
                new FeedResponseEntity(tagNames: '[ "tag2", "tag4", "tag5", "tag6" ]'),
                new FeedResponseEntity(tagNames: '[ "tag7", "tag8", "tag9"]'),
                new FeedResponseEntity(tagNames: '[ "tag10", "tag11"]'),
                new FeedResponseEntity(tagNames: '[ "tag12", "tag13", "tag14", "tag15" ]'),
                new FeedResponseEntity(tagNames: '[ "tag16", "tag17", "tag18", "tag19" ]'),
                new FeedResponseEntity(tagNames: '[ "tag20", "tag21", "tag22", "tag23" ]'),
        ]
        // tag2, tag3, tag4, tag5는 2번 이상 등장, 나머지는 전부 1번 등장
        def processor = batchConfig.viewedTagsProcessor()
        def memberId = 1L;
        when:
        def result = processor.process(memberId)
        then:
        result.viewedTags().size() == PageSize.FREQUENT_TAGS_SIZE
        result.viewedTags().containsAll(["tag2", "tag3", "tag4", "tag5"])
        result.memberId() == 1L
    }

    def "randomFeedsOfTagProcessor 테스트"() {
        given:
        feedRepository.findRandomFeedIdsByTagName(_) >> [1L,2L,3L]
        def processor = batchConfig.randomFeedsOfTagProcessor()
        when:
        def result = processor.process(new FrequentTags(1L, ["t1","t2","t3"]))
        then:
        result.memberId() == 1L
        result.feedIds().containsAll([1L,2L,3L])
    }
}
