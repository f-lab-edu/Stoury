package com.stoury.controller


import com.stoury.dto.SimpleMemberResponse
import com.stoury.dto.chat.ChatMessageResponse
import com.stoury.dto.chat.ChatRoomResponse
import com.stoury.dto.chat.SenderResponse
import com.stoury.dto.member.AuthenticatedMember
import com.stoury.service.ChatService
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType

import java.time.LocalDateTime

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
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

    def "Enter chat room"() {
        given:
        def parameterDescriptor = parameterWithName("chatRoomId").description("id of chat room")
        def member = new AuthenticatedMember(1, "test@email.com", "pwdpwdpwd123")
        when:
        def response = mockMvc.perform(get("/chatRoom/{chatRoomId}", "2")
                .with(authenticatedMember(member))
                .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andDo(documentWithPath(parameterDescriptor))
        then:
        response.andExpect(status().isOk())
    }

    def "Send chat message"() {
        given:
        def parameterDescriptor = parameterWithName("chatRoomId").description("id of chat room")
        def member = new AuthenticatedMember(1, "test@email.com", "pwdpwdpwd123")
        chatService.sendChatMessage(1, 2, "Hello, World!") >> new ChatMessageResponse(
                20, 2, new SenderResponse(1, "sender", null), "Hello, World!",
                LocalDateTime.of(2024, 12, 31, 13, 30, 14)
        )
        when:
        def response = mockMvc.perform(post("/chats/{chatRoomId}", "2")
                .with(authenticatedMember(member))
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, World!"))
                .andDo(documentWithPath(parameterDescriptor))
        then:
        response.andExpect(status().isOk())
    }

    def "Load more chats"() {
        given:
        def pathParameterDescriptor = parameterWithName("chatRoomId").description("id of chat room")
        def queryParameterDescriptor = parameterWithName("orderThan").description("Load chat logs order than this value").optional()
        def member = new AuthenticatedMember(1, "test@email.com", "pwdpwdpwd123")
        chatService.getPreviousChatMessages(1,2,_ as LocalDateTime) >>
            [
                    new ChatMessageResponse(3, 2, new SenderResponse(1, "member1", null),
                            "Hi!", LocalDateTime.of(2024, 12,31,13,30,5)),
                    new ChatMessageResponse(4, 2, new SenderResponse(2, "member2", null),
                            "Hi, member1!", LocalDateTime.of(2024, 12,31,13,30,7)),
                    new ChatMessageResponse(5, 2, new SenderResponse(1, "member1", null),
                            "How are you?", LocalDateTime.of(2024, 12,31,13,30,25)),
                    new ChatMessageResponse(6, 2, new SenderResponse(2, "member2", null),
                            "I'm fine, thank you", LocalDateTime.of(2024, 12,31,13,31,2))
            ]
        when:
        def response = mockMvc.perform(get("/chats/{chatRoomId}", "2")
                .with(authenticatedMember(member))
                .queryParam("orderThan", "2024-12-31T13:32:00"))
                .andDo(documentWithPathAndQuery(pathParameterDescriptor, queryParameterDescriptor))
        then:
        response.andExpect(status().isOk())
    }
}
