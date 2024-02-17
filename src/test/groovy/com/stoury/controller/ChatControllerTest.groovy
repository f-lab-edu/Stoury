package com.stoury.controller


import com.stoury.dto.SimpleMemberResponse
import com.stoury.dto.chat.ChatRoomResponse
import com.stoury.dto.member.AuthenticatedMember
import com.stoury.service.ChatService
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ChatController.class)
class ChatControllerTest extends AbstractRestDocsTests {
    @SpringBean
    ChatService chatService = Mock()

    def "Create chat room"() {
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
}
