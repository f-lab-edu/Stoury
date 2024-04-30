package com.stoury.controller

import com.stoury.dto.member.AuthenticatedMember
import com.stoury.service.LikeService
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(LikeController.class)
class LikeControllerTest extends AbstractRestDocsTests {
    @MockBean
    LikeService likeService

    def "Like a feed"() {
        given:
        def parameterDescriptor = parameterWithName("feedId").description("id of feed")
        def liker = new AuthenticatedMember(1L, "test@email.com", "pwdpwdpwd123")
        when:
        def response = mockMvc.perform(post("/like/feed/{feedId}", "1")
                .with(authenticatedMember(liker)))
                .andDo(documentWithPath(parameterDescriptor))
        then:
        response.andExpect(status().isOk())
    }

    def "Cancel like"() {
        given:
        def parameterDescriptor = parameterWithName("feedId").description("id of feed")
        def liker = new AuthenticatedMember(1L, "test@email.com", "pwdpwdpwd123")
        when:
        def response = mockMvc.perform(delete("/like/feed/{feedId}", "1")
                .with(authenticatedMember(liker)))
                .andDo(documentWithPath(parameterDescriptor))
        then:
        response.andExpect(status().isOk())
    }

    def "Check like or not"() {
        given:
        def parameterDescriptor = parameterWithName("feedId").description("id of feed")
        def liker = new AuthenticatedMember(1L, "test@email.com", "pwdpwdpwd123")
        when:
        def response = mockMvc.perform(get("/like/feed/{feedId}", "1")
                .with(authenticatedMember(liker)))
                .andDo(documentWithPath(parameterDescriptor))
        then:
        response.andExpect(status().isOk())
    }
}
