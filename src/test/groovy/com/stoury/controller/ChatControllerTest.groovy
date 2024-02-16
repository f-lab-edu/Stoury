package com.stoury.controller

import com.stoury.config.AuthorizationHeaderFilter
import com.stoury.dto.SimpleMemberResponse
import com.stoury.dto.chat.ChatRoomResponse
import com.stoury.dto.member.AuthenticatedMember
import com.stoury.service.ChatService
import com.stoury.utils.JwtUtils
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.filter.CharacterEncodingFilter

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ChatController.class)
class ChatControllerTest extends AbstractRestDocsTests {
    @SpringBean
    ChatService chatService = Mock()
    def authorizationHeaderFilter

    def setup() {
        def jwtUtils = new JwtUtils()
        jwtUtils.tokenSecret = "tokenSecret"
        authorizationHeaderFilter = Spy(new AuthorizationHeaderFilter(jwtUtils))
    }

    def "채팅방 생성"() {
        given:
        def parameterDescriptor = parameterWithName("receiverId").description("who receive first chat")
        def sender = new AuthenticatedMember(1, "test@email.com", "pwdpwdpwd123")
        chatService.createChatRoom(1, 2) >> new ChatRoomResponse(1,
                [new SimpleMemberResponse(1, "member1"),
                 new SimpleMemberResponse(2, "member2")])
        when:
        def response = mockMvc.perform(post("/chats/to/{receiverId}", "2")
                .with(authenticatedMember(sender)))
                .andDo(documentWithPath(parameterDescriptor))
        then:
        response.andExpect(status().isOk())
    }

    def "채팅방 권한 얻어오기"() {
        given:
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .alwaysDo(print())
                .addFilters(
                        new CharacterEncodingFilter("UTF-8", true),
                        authorizationHeaderFilter)
                .build()
        def parameterDescriptor = parameterWithName("chatRoomId").description("id of chat room")
        def member = new AuthenticatedMember(1, "test@email.com", "pwdpwdpwd123")
        authorizationHeaderFilter.getAuthenticatedMember(_) >> member
        when:
        def response = mockMvc.perform(get("/chatRoom/auth/{chatRoomId}", "1")
                .servletPath("/chatRoom/auth/1")
                .with(authenticatedMember(member)))
                .andDo(documentWithPath(parameterDescriptor))
        then:
        response.andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
    }
}
