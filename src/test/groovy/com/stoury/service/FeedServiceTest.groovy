package com.stoury.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.stoury.domain.ClickLog
import com.stoury.domain.Feed
import com.stoury.domain.GraphicContent
import com.stoury.domain.Member
import com.stoury.domain.Tag
import com.stoury.dto.feed.LocationResponse
import com.stoury.dto.feed.FeedCreateRequest
import com.stoury.dto.feed.FeedUpdateRequest
import com.stoury.event.FeedResponseCreateEvent
import com.stoury.event.FeedResponseDeleteEvent
import com.stoury.event.FeedResponseUpdateEvent
import com.stoury.event.GraphicDeleteEvent
import com.stoury.exception.authentication.NotAuthorizedException
import com.stoury.exception.feed.FeedCreateException
import com.stoury.projection.FeedResponseEntity
import com.stoury.repository.ClickLogRepository
import com.stoury.repository.FeedRepository
import com.stoury.repository.LikeRepository
import com.stoury.repository.MemberRepository
import com.stoury.repository.RecommendFeedsRepository
import com.stoury.service.location.LocationService
import com.stoury.service.storage.StorageService
import com.stoury.utils.JsonMapper
import org.springframework.context.ApplicationEventPublisher
import org.springframework.mock.web.MockMultipartFile
import spock.lang.Specification

class FeedServiceTest extends Specification {
    def storageService = Mock(StorageService)
    def memberRepository = Mock(MemberRepository)
    def tagService = Mock(TagService)
    def feedRepository = Mock(FeedRepository)
    def likeRepository = Mock(LikeRepository)
    def eventPublisher = Mock(ApplicationEventPublisher)
    def locationService = Mock(LocationService)
    def jsonMapper = new JsonMapper(new ObjectMapper())
    def recommendFeedRepository = Mock(RecommendFeedsRepository)
    def clickLogRepository = Mock(ClickLogRepository)
    def feedService = new FeedService(storageService, feedRepository, memberRepository, likeRepository,
            tagService, locationService, eventPublisher, jsonMapper, recommendFeedRepository, clickLogRepository)

    def writer = Mock(Member)
    def feedCreateRequest = FeedCreateRequest.builder()
            .textContent("testing")
            .longitude(111.111)
            .latitude(333.333)
            .build()
    def graphicContents = List.of(
            new MockMultipartFile("Files", "first", "image/jpeg", new byte[0]),
            new MockMultipartFile("Files", "second", "video/mp4", new byte[0])
    )
    def savedFeed = Feed.builder()
            .member(writer)
            .textContent(feedCreateRequest.textContent())
            .latitude(11.11)
            .longitude(22.22)
            .tags([] as Set)
            .build()

    def setup() {
        memberRepository.findById(_) >> Optional.of(writer)
    }

    def "피드 생성 성공"() {
        when:
        feedService.createFeed(1L, feedCreateRequest, graphicContents)
        then:
        1 * feedRepository.save(_ as Feed) >> savedFeed
        1 * locationService.getLocation(_, _) >> new LocationResponse("city", "country")
        1 * eventPublisher.publishEvent(_ as FeedResponseCreateEvent)
    }

    def "피드 생성 실패, 지원하지 않는 파일"() {
        given:
        def notSupportedContents = List.of(
                new MockMultipartFile("Files", "first", "image/jpeg", new byte[0]),
                new MockMultipartFile("Files", "second", "video/mp4", new byte[0]),
                new MockMultipartFile("Files", "third", "image/png", new byte[0])
        )
        when:
        feedService.createFeed(1L, feedCreateRequest, notSupportedContents)
        then:
        thrown(FeedCreateException.class)
    }

    def "피드 저장 실패, 이미지 없음"() {
        given:
        def emptyContents = Collections.emptyList()
        when:
        feedService.createFeed(1L, feedCreateRequest, emptyContents)
        then:
        thrown(FeedCreateException.class)
    }

    def "피드 저장 실패, 위치정보 없음"() {
        when:
        FeedCreateRequest.builder().build()
        then:
        thrown(FeedCreateException.class)
    }

    def "피드 업데이트 성공"() {
        given:
        def feed = new Feed(writer, "before updated", 11.11, 22.22,
                [Mock(Tag)] as Set, "city", "country")
        feed.id = 1L
        feed.graphicContents = new ArrayList<>(List.of(
                new GraphicContent("path1", 0),
                new GraphicContent("path2", 1),
                new GraphicContent("path3", 2),
                new GraphicContent("path4", 3),
                new GraphicContent("path5", 4)))
        feedRepository.findById(_ as Long) >> Optional.of(feed)

        def feedUpdateRequest = new FeedUpdateRequest("updated", [] as Set, Set.of(1, 3))
        when:
        feedService.updateFeed(1L, feedUpdateRequest)
        then:
        2 * eventPublisher.publishEvent(_ as GraphicDeleteEvent)
        1 * eventPublisher.publishEvent(_ as FeedResponseUpdateEvent)
        feed.textContent == "updated"
        feed.tags.isEmpty()
    }

    def "피드 삭제 실패 - 소유권없음"() {
        given:
        def feed = Mock(Feed)
        feedRepository.findById(_) >> Optional.of(feed)
        feed.notOwnedBy(1) >> true
        when:
        feedService.deleteFeedIfOwner(1, 1)
        then:
        thrown(NotAuthorizedException)
    }

    def "피드 삭제 성공"() {
        given:
        def writer = new Member(id: 1)
        def feed = new Feed(id: 1, member: writer, graphicContents: [new GraphicContent("/gc1", 0)])
        feedRepository.findById(_) >> Optional.of(feed)
        when:
        feedService.deleteFeedIfOwner(feed.id, writer.id)
        then:
        1 * feedRepository.delete(_)
        1 * eventPublisher.publishEvent(_ as GraphicDeleteEvent)
        1 * eventPublisher.publishEvent(_ as FeedResponseDeleteEvent)
    }

    def "사용자의 피드 조회 성공"() {
        given:
        def feeds = [
                new FeedResponseEntity(1L, writer.id, writer.username,  '[{"id":null, "path":null}]',  "[null]",null,"", 0.0, 0.0,"", ""),
                new FeedResponseEntity(2L, writer.id, writer.username,  '[{"id":null, "path":null}]',  "[null]",null,"", 0.0, 0.0,"", ""),
                new FeedResponseEntity(3L, writer.id, writer.username,  '[{"id":null, "path":null}]',  "[null]",null,"", 0.0, 0.0,"", ""),
                new FeedResponseEntity(4L, writer.id, writer.username,  '[{"id":null, "path":null}]',  "[null]",null,"", 0.0, 0.0,"", ""),
                new FeedResponseEntity(5L, writer.id, writer.username,  '[{"id":null, "path":null}]',  "[null]",null,"", 0.0, 0.0,"", ""),
                new FeedResponseEntity(6L, writer.id, writer.username,  '[{"id":null, "path":null}]',  "[null]",null,"", 0.0, 0.0,"", ""),
                new FeedResponseEntity(7L, writer.id, writer.username,  '[{"id":null, "path":null}]',  "[null]",null,"", 0.0, 0.0,"", ""),
                new FeedResponseEntity(8L, writer.id, writer.username,  '[{"id":null, "path":null}]',  "[null]",null,"", 0.0, 0.0,"", ""),
                new FeedResponseEntity(9L, writer.id, writer.username,  '[{"id":null, "path":null}]',  "[null]",null,"", 0.0, 0.0,"", ""),
                new FeedResponseEntity(10L, writer.id, writer.username,  '[{"id":null, "path":null}]',  "[null]",null,"", 0.0, 0.0,"", ""),
        ]
        when:
        feedService.getFeedsOfMemberId(1L, 20L)
        then:
        1 * feedRepository.findAllFeedsByMemberAndIdLessThan(_, _, _) >> feeds
    }

    def "개인 맞춤 피드 제공 성공"(){
        given:
        def feedIds = [1L,2L,4L,5L]
        recommendFeedRepository.findAllByMemberId(_ as Long) >> feedIds
        when:
        feedService.getRecommendedFeeds(1L)
        then:
        1 * feedRepository.findAllFeedsByIdIn(feedIds) >> []
    }

    def "사용자가 피드를 조회시 기록을 남김"(){
        given:
        feedRepository.findById(_ as Long) >> Optional.of(new Feed())
        when:
        feedService.clickLogUpdate(1L,1L)
        then:
        1 * clickLogRepository.save(_ as ClickLog)
    }
}
