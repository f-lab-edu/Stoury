package com.stoury.controller

import com.stoury.domain.GraphicContent
import com.stoury.dto.SimpleMemberResponse
import com.stoury.dto.feed.FeedCreateRequest
import com.stoury.dto.feed.FeedResponse
import com.stoury.dto.feed.FeedUpdateRequest
import com.stoury.dto.feed.GraphicContentResponse
import com.stoury.dto.feed.LocationResponse
import com.stoury.dto.member.AuthenticatedMember
import com.stoury.service.FeedService
import com.stoury.utils.JsonMapper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile

import java.time.LocalDateTime

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.when
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(FeedController.class)
class FeedControllerTest extends AbstractRestDocsTests {
    @SpringBean
    FeedService feedService = Mock()
    @Autowired
    JsonMapper jsonMapper

    def "Create Feed"() {
        given:
        def writer = new AuthenticatedMember(1L, "test@email.com", "pwdpwd1111")
        def feedCreateRequest = FeedCreateRequest.builder()
                .textContent("This is content")
                .latitude(36.5116)
                .longitude(127.2359)
                .tagNames(["korea", "travel"] as Set)
                .build()
        List<MultipartFile> graphicContents = [
                new MockMultipartFile("images", "file1.jpeg", "image/jpeg", new byte[0]),
                new MockMultipartFile("images", "file2.mp4", "video/mp4", new byte[0]),
                new MockMultipartFile("images", "file3.jpeg", "image/jpeg", new byte[0]),
        ]
        feedService.createFeed(_ as Long, _ as FeedCreateRequest, _ as List) >>
                new FeedResponse(
                        1L,
                        new SimpleMemberResponse(writer.getId(), "testWriter"),
                        [
                                new GraphicContentResponse(1, "/" + graphicContents.get(0).getOriginalFilename()),
                                new GraphicContentResponse(2, "/" + graphicContents.get(1).getOriginalFilename()),
                                new GraphicContentResponse(3, "/" + graphicContents.get(2).getOriginalFilename()),
                        ],
                        feedCreateRequest.textContent(),
                        feedCreateRequest.latitude(),
                        feedCreateRequest.longitude(),
                        feedCreateRequest.tagNames(),
                        new LocationResponse("sejong-si", "Republic of Korea"),
                        0,
                        LocalDateTime.of(2024, 12, 31, 13, 30, 20, 14)
                )
        MockMultipartFile feedCreateRequestPart = new MockMultipartFile("feedCreateRequest", "", "application/json", jsonMapper.getJsonBytes(feedCreateRequest));

        when:
        def response = mockMvc.perform(multipart("/feeds")
                .file(graphicContents.get(0))
                .file(graphicContents.get(1))
                .file(graphicContents.get(2))
                .file(feedCreateRequestPart)
                .with(authenticatedMember(writer)))
                .andDo(document())

        then:
        response.andExpect(status().isOk())
    }

    def "Get a feed"() {
        given:
        def parameterDescriptor = parameterWithName("feedId").description("id of feed")
        feedService.getFeed(1L) >> new FeedResponse(
                1L,
                new SimpleMemberResponse(1L, "testWriter"),
                [
                        new GraphicContentResponse(1, "/file1.jpeg"),
                        new GraphicContentResponse(2, "/file2.jpeg"),
                        new GraphicContentResponse(3, "/file3.jpeg"),
                ],
                "This is content",
                36.5116,
                127.2359,
                ["korea", "travel"] as Set,
                new LocationResponse("sejong-si", "Republic of Korea"),
                0,
                LocalDateTime.of(2024, 12, 31, 13, 30, 20, 14)
        )

        when:
        def response = mockMvc.perform(get("/feeds/{feedId}", "1"))
                .andDo(documentWithPath(parameterDescriptor))

        then:
        response.andExpect(status().isOk())
    }

    def "Get feeds by tag"() {
        given:
        def parameterDescriptor = parameterWithName("tagName").description("name of tag")
        def queryDescriptor = parameterWithName("offsetId")
                .description("Results which created order than whose id is offsetId").optional()

        feedService.getFeedsByTag(_, _) >> [
                new FeedResponse(
                        2L,
                        new SimpleMemberResponse(1L, "testWriter"),
                        [
                                new GraphicContentResponse(1, "/file1.jpeg"),
                                new GraphicContentResponse(2, "/file2.jpeg"),
                                new GraphicContentResponse(3, "/file3.jpeg"),
                        ],
                        "This is content",
                        36.5116,
                        127.2359,
                        ["korea", "travel"] as Set,
                        new LocationResponse("sejong-si", "Republic of Korea"),
                        0,
                        LocalDateTime.of(2024, 12, 31, 13, 30, 20, 14)
                ),
                new FeedResponse(
                        1L,
                        new SimpleMemberResponse(2L, "testWriter"),
                        Collections.emptyList(),
                        "This is content2",
                        36.3157,
                        127.3913,
                        ["daejeon", "travel"] as Set,
                        new LocationResponse("daejeon-si", "Republic of Korea"),
                        0,
                        LocalDateTime.of(2024, 12, 31, 13, 30, 20, 14)
                )
        ]
        when:
        def response = mockMvc.perform(get("/feeds/tag/{tagName}", "travel")
                .param("offsetId", "4"))
                .andDo(documentWithPathAndQuery(parameterDescriptor, queryDescriptor))
        then:
        response.andExpect(status().isOk())
    }

    def "Get feeds of member"() {
        given:
        def parameterDescriptor = parameterWithName("memberId").description("id of member")
        def queryDescriptor = parameterWithName("offsetId")
                .description("Results which created order than whose id is offsetId").optional()

        feedService.getFeedsOfMemberId(_, _) >> [
                new FeedResponse(
                        2L,
                        new SimpleMemberResponse(1L, "testWriter"),
                        [
                                new GraphicContentResponse(1, "/file1.jpeg"),
                                new GraphicContentResponse(2, "/file2.jpeg"),
                                new GraphicContentResponse(3, "/file3.jpeg"),
                        ],
                        "This is content",
                        36.5116,
                        127.2359,
                        ["korea", "travel"] as Set,
                        new LocationResponse("sejong-si", "Republic of Korea"),
                        0,
                        LocalDateTime.of(2024, 12, 31, 13, 30, 20, 14)
                ),
                new FeedResponse(
                        1L,
                        new SimpleMemberResponse(1L, "testWriter"),
                        Collections.emptyList(),
                        "This is content2",
                        36.3157,
                        127.3913,
                        ["daejeon", "travel"] as Set,
                        new LocationResponse("daejeon-si", "Republic of Korea"),
                        0,
                        LocalDateTime.of(2024, 12, 31, 13, 30, 20, 14)
                )
        ]
        when:
        def response = mockMvc.perform(get("/feeds/member/{memberId}", "1")
                .param("offsetId", "4"))
                .andDo(documentWithPathAndQuery(parameterDescriptor, queryDescriptor))
        then:
        response.andExpect(status().isOk())
    }

    def "Update a feed"() {
        given:
        def parameterDescriptor = parameterWithName("feedId").description("id of feed")

        def writer = new AuthenticatedMember(1L, "test@email.com", "pwdpwd1111")
        def feedUpdateRequest = new FeedUpdateRequest(
                "Updated content",
                ["New", "Updated"] as Set,
                Set.of(1, 3))
        feedService.updateFeedIfOwner(_,_,_) >> new FeedResponse(
                1L,
                new SimpleMemberResponse(writer.getId(), "testWriter"),
                [
                        new GraphicContentResponse(2, "/file2.jpeg"),
                        new GraphicContentResponse(4, "/file4.jpeg"),
                        new GraphicContentResponse(5, "/file5.jpeg"),
                ],
                feedUpdateRequest.textContent(),
                36.5116,
                127.2359,
                feedUpdateRequest.tagNames(),
                new LocationResponse("daejeon-si", "Republic of Korea"),
                0,
                LocalDateTime.of(2024, 12, 31, 13, 30, 20, 14)
        )
        when:
        def response = mockMvc.perform(put("/feeds/{feedId}", "1")
                .content(jsonMapper.getJsonString(feedUpdateRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .with(authenticatedMember(writer)))
                .andDo(documentWithPath(parameterDescriptor))

        then:
        response.andExpect(status().isOk())
    }

    def "Delete a feed"() {
        given:
        def writer = new AuthenticatedMember(1L, "test@email.com", "pwdpwd1111")
        def parameterDescriptor = parameterWithName("feedId").description("id of feed")
        when:
        def response = mockMvc.perform(delete("/feeds/{feedId}", "1")
                .with(authenticatedMember(writer)))
                .andDo(documentWithPath(parameterDescriptor))
        then:
        response.andExpect(status().isOk())
    }

    def "Get recommend feeds"() {
        given:
        def writer = new AuthenticatedMember(1L, "test@email.com", "pwdpwd1111")
        feedService.getRecommendedFeeds(_) >> [
                new FeedResponse(10L, new SimpleMemberResponse(2L, "tester2"),
                        [
                                new GraphicContentResponse(20L, "/file1.jpeg"),
                                new GraphicContentResponse(21L, "/file2.jpeg"),
                        ],
                        "recommend feed1",
                        36.5,
                        127.5,
                        ["tag1", "tag2", "tag3"] as Set,
                        new LocationResponse("city1", "Country1"),
                        3, LocalDateTime.of(2024, 12, 31, 0, 0)),
                new FeedResponse(233L, new SimpleMemberResponse(3L, "tester3"),
                        [
                                new GraphicContentResponse(45L, "/file3.jpeg"),
                                new GraphicContentResponse(46L, "/file4.jpeg"),
                        ],
                        "recommend feed2",
                        36.5,
                        127.5,
                        ["tag5", "tag2", "tag3"] as Set,
                        new LocationResponse("city1", "Country1"),
                        99, LocalDateTime.of(2024, 12, 31, 0, 0)),
                new FeedResponse(3456L, new SimpleMemberResponse(3L, "tester3"),
                        [
                                new GraphicContentResponse(99L, "/file6.jpeg"),
                                new GraphicContentResponse(100L, "/file7.jpeg"),
                        ],
                        "recommend feed3",
                        36.5,
                        127.5,
                        ["tag1", "tag2", "tag99"] as Set,
                        new LocationResponse("city1", "Country1"),
                        3, LocalDateTime.of(2024, 12, 31, 0, 0)),
        ]
        when:
        def response = mockMvc.perform(get("/feeds/recommend")
                .with(authenticatedMember(writer)))
                .andDo(document())
        then:
        response.andExpect(status().isOk())
    }

    def "Update viewed feeds"() {
        given:
        def writer = new AuthenticatedMember(1L, "test@email.com", "pwdpwd1111")
        def parameterDescriptor = parameterWithName("feedId").description("id of feed")
        when:
        def response = mockMvc.perform(post("/feeds/viewed/{feedId}", "1")
                .with(authenticatedMember(writer)))
                .andDo(documentWithPath(parameterDescriptor))
        then:
        response.andExpect(status().isOk())
    }

    def "Get follower view based recommend feeds"() {
        given:
        def writer = new AuthenticatedMember(1L, "test@email.com", "pwdpwd1111")
        feedService.getFollowerViewedRecommendFeeds(1L) >> [
                new FeedResponse(10L, new SimpleMemberResponse(2L, "tester2"),
                        [
                                new GraphicContentResponse(20L, "/file1.jpeg"),
                                new GraphicContentResponse(21L, "/file2.jpeg"),
                        ],
                        "recommend feed1",
                        36.5,
                        127.5,
                        ["tag1", "tag2", "tag3"] as Set,
                        new LocationResponse("city1", "Country1"),
                        3, LocalDateTime.of(2024, 12, 31, 0, 0)),
                new FeedResponse(233L, new SimpleMemberResponse(3L, "tester3"),
                        [
                                new GraphicContentResponse(45L, "/file3.jpeg"),
                                new GraphicContentResponse(46L, "/file4.jpeg"),
                        ],
                        "recommend feed2",
                        36.5,
                        127.5,
                        ["tag5", "tag2", "tag3"] as Set,
                        new LocationResponse("city1", "Country1"),
                        99, LocalDateTime.of(2024, 12, 31, 0, 0)),
                new FeedResponse(3456L, new SimpleMemberResponse(3L, "tester3"),
                        [
                                new GraphicContentResponse(99L, "/file6.jpeg"),
                                new GraphicContentResponse(100L, "/file7.jpeg"),
                        ],
                        "recommend feed3",
                        36.5,
                        127.5,
                        ["tag1", "tag2", "tag99"] as Set,
                        new LocationResponse("city1", "Country1"),
                        3, LocalDateTime.of(2024, 12, 31, 0, 0)),
        ]
        when:
        def response = mockMvc.perform(get("/feeds/follower-viewed")
                .with(authenticatedMember(writer)))
                .andDo(document())
        then:
        response.andExpect(status().isOk())
    }
}
