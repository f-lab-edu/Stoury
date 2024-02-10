package com.stoury.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.stoury.dto.WriterResponse
import com.stoury.dto.feed.FeedCreateRequest
import com.stoury.dto.feed.FeedResponse
import com.stoury.dto.feed.LocationResponse
import com.stoury.dto.member.AuthenticatedMember
import com.stoury.service.FeedService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.request.RequestPostProcessor
import org.springframework.web.multipart.MultipartFile

import java.time.LocalDateTime

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.when
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(FeedController.class)
class FeedControllerTest extends AbstractRestDocsTests {
    @MockBean
    FeedService feedService
    @Autowired
    ObjectMapper objectMapper

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
                        new WriterResponse(writer.getId(), "testWriter"),
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
        MockMultipartFile feedCreateRequestPart = new MockMultipartFile("feedCreateRequest", "", "application/json", objectMapper.writeValueAsBytes(feedCreateRequest));

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
}
