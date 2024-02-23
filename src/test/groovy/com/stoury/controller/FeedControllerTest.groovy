package com.stoury.controller

import com.stoury.dto.SimpleMemberResponse
import com.stoury.dto.feed.FeedCreateRequest
import com.stoury.dto.feed.FeedResponse
import com.stoury.dto.feed.FeedUpdateRequest
import com.stoury.dto.feed.LocationResponse
import com.stoury.dto.member.AuthenticatedMember
import com.stoury.service.FeedService
import com.stoury.utils.JsonMapper
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
    @MockBean
    FeedService feedService
    @Autowired
    JsonMapper jsonMapper

    def "Create Feed"() {
        given:
        def writer = new AuthenticatedMember(1L, "test@email.com", "pwdpwd1111")
        def feedCreateRequest = FeedCreateRequest.builder()
                .textContent("This is content")
                .latitude(36.5116)
                .longitude(127.2359)
                .tagNames(List.of("korea", "travel"))
                .build()
        List<MultipartFile> graphicContents = List.of(
                new MockMultipartFile("images", "file1.jpeg", "image/jpeg", new byte[0]),
                new MockMultipartFile("images", "file2.mp4", "video/mp4", new byte[0]),
                new MockMultipartFile("images", "file3.jpeg", "image/jpeg", new byte[0]),
        )
        when(feedService.createFeed(any(Long.class), any(FeedCreateRequest.class), any(List))).thenReturn(
                new FeedResponse(
                        1L,
                        new SimpleMemberResponse(writer.getId(), "testWriter"),
                        graphicContents.stream().map(file -> "/" + file.getOriginalFilename()).toList(),
                        feedCreateRequest.textContent(),
                        feedCreateRequest.latitude(),
                        feedCreateRequest.longitude(),
                        feedCreateRequest.tagNames(),
                        new LocationResponse("sejong-si", "Republic of Korea"),
                        0,
                        LocalDateTime.of(2024, 12, 31, 13, 30, 20, 14)
                )
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
        when(feedService.getFeed(1L)).thenReturn(
                new FeedResponse(
                        1L,
                        new SimpleMemberResponse(1L, "testWriter"),
                        List.of("/file1.jpeg", "/file2.mp4", "/file3.jpeg"),
                        "This is content",
                        36.5116,
                        127.2359,
                        List.of("korea", "travel"),
                        new LocationResponse("sejong-si", "Republic of Korea"),
                        0,
                        LocalDateTime.of(2024, 12, 31, 13, 30, 20, 14)
                )
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
        def queryDescriptor = parameterWithName("orderThan")
                .description("Results which created orderThan this value are listed")
                .optional()


        when(feedService.getFeedsByTag(any(), any())).thenReturn(List.of(
                new FeedResponse(
                        1L,
                        new SimpleMemberResponse(1L, "testWriter"),
                        List.of("/file1.jpeg", "/file2.mp4", "/file3.jpeg"),
                        "This is content",
                        36.5116,
                        127.2359,
                        List.of("korea", "travel"),
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
                        List.of("daejeon", "travel"),
                        new LocationResponse("daejeon-si", "Republic of Korea"),
                        0,
                        LocalDateTime.of(2024, 12, 31, 13, 30, 20, 14)
                )
        ))
        when:
        def response = mockMvc.perform(get("/feeds/tag/{tagName}", "travel")
                .param("orderThan", "2024-12-31T15:00:00"))
                .andDo(documentWithPathAndQuery(parameterDescriptor, queryDescriptor))
        then:
        response.andExpect(status().isOk())
    }

    def "Get feeds of member"() {
        given:
        def parameterDescriptor = parameterWithName("memberId").description("id of member")
        def queryDescriptor = parameterWithName("orderThan")
                .description("Results which created orderThan this value are listed")
                .optional()


        when(feedService.getFeedsOfMemberId(any(), any())).thenReturn(List.of(
                new FeedResponse(
                        1L,
                        new SimpleMemberResponse(1L, "testWriter"),
                        List.of("/file1.jpeg", "/file2.mp4", "/file3.jpeg"),
                        "This is content",
                        36.5116,
                        127.2359,
                        List.of("korea", "travel"),
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
                        List.of("daejeon", "travel"),
                        new LocationResponse("daejeon-si", "Republic of Korea"),
                        0,
                        LocalDateTime.of(2024, 12, 31, 13, 30, 20, 14)
                )
        ))
        when:
        def response = mockMvc.perform(get("/feeds/member/{memberId}", "1")
                .param("orderThan", "2024-12-31T15:00:00"))
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
                List.of("New", "Updated"),
                Set.of(1, 3))
        when(feedService.updateFeedIfOwner(any(), any(), any())).thenReturn(
                new FeedResponse(
                        1L,
                        new SimpleMemberResponse(writer.getId(), "testWriter"),
                        List.of("/file2.jpeg", "/file4.jpeg", "/file5.jpeg"),
                        feedUpdateRequest.textContent(),
                        36.5116,
                        127.2359,
                        feedUpdateRequest.tagNames(),
                        new LocationResponse("daejeon-si", "Republic of Korea"),
                        0,
                        LocalDateTime.of(2024, 12, 31, 13, 30, 20, 14)
                )
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
}
