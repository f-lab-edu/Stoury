package com.stoury.controller

import com.stoury.dto.SimpleMemberResponse
import com.stoury.dto.member.AuthenticatedMember
import com.stoury.service.FollowService
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(FollowController.class)
class FollowControllerTest extends AbstractRestDocsTests {
    @SpringBean
    FollowService followService = Mock()

    def "Follow member"() {
        given:
        def follower = new AuthenticatedMember(1L, "follower@enail.com", "123123123")
        when:
        def response = mockMvc.perform(post("/follow")
                .contentType(MediaType.TEXT_PLAIN)
                .content("followee@email.com")
                .with(authenticatedMember(follower)))
                .andDo(document())
        then:
        response.andExpect(status().isOk())
    }

    def "Get following members"() {
        given:
        def follower = new AuthenticatedMember(1L, "follower@enail.com", "123123123")
        followService.getFollowingMembers(_) >> [
                new SimpleMemberResponse(3L, "followee3@email.com"),
                new SimpleMemberResponse(4L, "followee4@email.com"),
                new SimpleMemberResponse(5L, "followee5@email.com"),
        ]
        when:
        def response = mockMvc.perform(get("/following")
                .with(authenticatedMember(follower)))
                .andDo(document())
        then:
        response.andExpect(status().isOk())
    }

    def "Get followers of member"() {
        given:
        def queryParameterDescriptor = queryParameters(parameterWithName("offsetUsername").description("username of offset"))
        def followee = new AuthenticatedMember(1L, "followee@enail.com", "123123123")
        followService.getFollowersOfMember(_, _) >> [
                new SimpleMemberResponse(3L, "follower3@email.com"),
                new SimpleMemberResponse(4L, "follower4@email.com"),
                new SimpleMemberResponse(5L, "follower5@email.com"),
        ]
        when:
        def response = mockMvc.perform(get("/followers")
                .param("offsetUsername", "gollower@email.com")
                .with(authenticatedMember(followee)))
                .andDo(documentWithQueryParameters(queryParameterDescriptor))
        then:
        response.andExpect(status().isOk())
    }
}
