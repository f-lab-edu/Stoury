package com.stoury.service

import com.stoury.domain.Feed
import com.stoury.domain.GraphicContent
import com.stoury.domain.Member
import com.stoury.domain.Tag
import com.stoury.dto.feed.LocationResponse
import com.stoury.dto.feed.FeedCreateRequest
import com.stoury.dto.feed.FeedUpdateRequest
import com.stoury.event.GraphicDeleteEvent
import com.stoury.exception.feed.FeedCreateException
import com.stoury.repository.FeedRepository
import com.stoury.repository.LikeRepository
import com.stoury.repository.MemberRepository
import com.stoury.service.location.LocationService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.mock.web.MockMultipartFile
import spock.lang.Specification

class FeedServiceTest extends Specification {
    def memberRepository = Mock(MemberRepository)
    def tagService = Mock(TagService)
    def feedRepository = Mock(FeedRepository)
    def likeRepository = Mock(LikeRepository)
    def eventPublisher = Mock(ApplicationEventPublisher)
    def locationService = Mock(LocationService)
    def feedService = new FeedService(feedRepository, memberRepository, likeRepository,
            tagService, locationService, eventPublisher)

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
            .tags(new ArrayList<>())
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
                List.of(Mock(Tag)), "city", "country")
        feed.id = 1L
        feed.graphicContents = new ArrayList<>(List.of(
                new GraphicContent("path1", 0),
                new GraphicContent("path2", 1),
                new GraphicContent("path3", 2),
                new GraphicContent("path4", 3),
                new GraphicContent("path5", 4)))
        feedRepository.findById(_ as Long) >> Optional.of(feed)

        def feedUpdateRequest = new FeedUpdateRequest("updated", Collections.emptyList(), Set.of(1, 3))
        when:
        feedService.updateFeed(1L, feedUpdateRequest)
        then:
        2 * eventPublisher.publishEvent(_ as GraphicDeleteEvent)
        feed.textContent == "updated"
        feed.tags.isEmpty()
    }
}
