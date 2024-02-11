package com.stoury.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.stoury.dto.WriterResponse
import com.stoury.dto.diary.DiaryCreateRequest
import com.stoury.dto.diary.DiaryPageResponse
import com.stoury.dto.diary.DiaryResponse
import com.stoury.dto.diary.SimpleDiaryResponse
import com.stoury.dto.feed.FeedResponse
import com.stoury.dto.feed.LocationResponse
import com.stoury.dto.member.AuthenticatedMember
import com.stoury.service.DiaryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType

import java.time.LocalDateTime

import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyInt
import static org.mockito.Mockito.when
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(DiaryController.class)
class DiaryControllerTest extends AbstractRestDocsTests {
    @MockBean
    DiaryService diaryService

    @Autowired
    ObjectMapper objectMapper

    def feed1 = new FeedResponse(1, new WriterResponse(1, "writer1"),
            List.of("/feed/images/image_1.jpeg"),
            "This is feed1", 40.0627, -105.1779, List.of("America", "denver"),
            new LocationResponse("Colorado", "United States"), 20, LocalDateTime.now())
    def feed2 = new FeedResponse(2, new WriterResponse(1, "writer1"),
            List.of("/feed/images/image_2.jpeg", "/feed/images/image_3.jpeg"),
            "This is feed2", 39.0484, -110.8451, List.of("America", "Utah", "Tavel"),
            new LocationResponse("Utah", "United States"), 13, LocalDateTime.now().plusDays(1))
    def feed3 = new FeedResponse(3, new WriterResponse(1, "writer1"),
            List.of("/feed/images/image_4.jpeg", "/feed/images/image_5.jpeg"),
            "This is feed3", 38.5269, -115.3801, List.of("Nevada", "UFO"),
            new LocationResponse("Nevada", "United States"), 52, LocalDateTime.now().plusDays(2))

    def "Create a diary"() {
        given:
        def writer = new AuthenticatedMember(1, "test@email.com", "pwdpwdpwd123")
        def diaryRequest = new DiaryCreateRequest("My Stoury", List.of(1L, 2L, 3L), 2)
        when(diaryService.createDiary(diaryRequest, 1)).thenReturn(new DiaryResponse(
                1L, 1L, diaryRequest.title(), feed1.graphicContentsPaths().get(0),
                Map.of(
                        1L, List.of(feed1),
                        2L, List.of(feed2, feed3)
                ),
                feed1.createdAt().toLocalDate(),
                feed3.createdAt().toLocalDate(),
                feed1.location().city(), feed1.location().country(),
                feed1.likes() + feed2.likes() + feed3.likes()
        ))
        when:
        def response = mockMvc.perform(post("/diaries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(diaryRequest))
                .with(authenticatedMember(writer)))
                .andDo(document())
        then:
        response.andExpect(status().isOk())
    }

    def "Get diaries of a member"() {
        given:
        def pathParameterDescriptor = parameterWithName("memberId").description("id of member")
        def queryParameterDescriptor = parameterWithName("pageNo").description("page number of diaries").optional()
        when(diaryService.getMemberDiaries(any(), anyInt()))
                .thenReturn(
                        new DiaryPageResponse(
                                List.of(
                                        new SimpleDiaryResponse(1, "/feed/images/image_14.jpeg", "My Stoury", 1),
                                        new SimpleDiaryResponse(2, "/feed/images/image_16.jpeg", "South Korea, Seoul, 2023-12-01~2024-01-12", 1),
                                        new SimpleDiaryResponse(3, "/feed/images/image_120.jpeg", "Turkiye travel with family", 1),
                                ),
                                0,
                                false
                        )
                )
        when:
        def response = mockMvc.perform(get("/diaries/member/{memberId}", "1"))
                .andDo(documentWithPathAndQuery(pathParameterDescriptor, queryParameterDescriptor))
        then:
        response.andExpect(status().isOk())
    }

    def "Delete a diary"() {
        given:
        def writer = new AuthenticatedMember(1, "test@email.com", "pwdpwdpwd123")
        def pathParameterDescriptor = parameterWithName("diaryId").description("id of diary")
        expect:
        mockMvc.perform(delete("/diaries/{diaryId}", "1")
                .with(authenticatedMember(writer)))
                .andDo(documentWithPath(pathParameterDescriptor))
                .andExpect(status().isOk())
    }
}
