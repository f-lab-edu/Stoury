package com.stoury.controller

import com.stoury.dto.member.AuthenticatedMember
import com.stoury.dto.member.MemberCreateRequest
import com.stoury.dto.member.MemberResponse
import com.stoury.dto.member.OnlineMember
import com.stoury.service.MemberService
import com.stoury.utils.JsonMapper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType

import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(MemberController.class)
class MemberControllerTest extends AbstractRestDocsTests {
    @SpringBean
    MemberService memberService = Mock()

    @Autowired
    JsonMapper jsonMapper

    def "JoinMember"() {
        given:
        def memberCreateRequest = MemberCreateRequest.builder()
                .username("testmember")
                .email("test@email.com")
                .password("password123123")
                .introduction("This is introduction")
                .build()
        def member = memberCreateRequest.toEntity("")
        member.id = 123L
        memberService.createMember(memberCreateRequest) >> MemberResponse.from(member)

        when:
        def response = mockMvc.perform(post("/members")
                .content(jsonMapper.getJsonString(memberCreateRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(document())

        then:
        response.andExpect(status().isOk())
    }

    def "Set status online"() {
        given:
        def member = new AuthenticatedMember(1, "test@email.com", "pwdpwdpwd123")
        def parameterDescriptors = queryParameters(
                parameterWithName("latitude").description("Current user's latitude").optional(),
                parameterWithName("longitude").description("Current user's longitude").optional(),
        )
        when:
        def response = mockMvc.perform(post("/members/set-online")
                .queryParam("latitude", "36.126")
                .queryParam("longitude", "127.125")
                .with(authenticatedMember(member)))
                .andDo(documentWithQueryParameters(parameterDescriptors))

        then:
        response.andExpect(status().isOk())
    }

    def "Set status offline"() {
        given:
        def member = new AuthenticatedMember(1, "test@email.com", "pwdpwdpwd123")
        when:
        def response = mockMvc.perform(post("/members/set-offline")
                .with(authenticatedMember(member)))
                .andDo(document())
        then:
        response.andExpect(status().isOk())
    }

    def "Search around online members"() {
        given:
        def queryParameters = queryParameters(
                parameterWithName("latitude").description("Current user's latitude"),
                parameterWithName("longitude").description("Current user's longitude"),
                parameterWithName("radiusKm").description("Search radius as kilometers.")
        )

        def member = new AuthenticatedMember(1, "test@email.com", "pwdpwdpwd123")
        def aroundMembers = List.of(
                new OnlineMember(3, "member3", "member3@email.com", 2),
                new OnlineMember(2, "member2", "member2@email.com", 10),
                new OnlineMember(4, "member4", "member4@email.com", 33),
                new OnlineMember(5, "member5", "member5@email.com", 47),
        )
        memberService.searchOnlineMembers(_, _, _, _) >> aroundMembers
        when:
        def response = mockMvc.perform(get("/members/around")
                .queryParam("latitude", "36.12345")
                .queryParam("longitude", "127.12345")
                .queryParam("radiusKm", "50")
                .with(authenticatedMember(member)))
                .andDo(documentWithQueryParameters(queryParameters))
        then:
        response.andExpect(status().isOk())
    }
}
