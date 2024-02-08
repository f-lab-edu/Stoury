package com.stoury.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.stoury.dto.member.MemberCreateRequest
import com.stoury.dto.member.MemberResponse
import com.stoury.service.MemberService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType

import static org.mockito.Mockito.when
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(MemberController.class)
class MemberControllerTest extends AbstractRestDocsTests {
    @MockBean
    MemberService memberService

    @Autowired
    ObjectMapper objectMapper

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
        when(memberService.createMember(memberCreateRequest)).thenReturn(MemberResponse.from(member))

        when:
        def response = mockMvc.perform(post("/members")
                .content(objectMapper.writeValueAsString(memberCreateRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(document())

        then:
        response.andExpect(status().isOk())
    }
}
