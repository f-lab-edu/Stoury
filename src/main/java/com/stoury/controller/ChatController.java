package com.stoury.controller;

import com.stoury.dto.chat.ChatMessageResponse;
import com.stoury.dto.chat.ChatRoomResponse;
import com.stoury.dto.member.AuthenticatedMember;
import com.stoury.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/chats/to/{receiverId}")
    public ChatRoomResponse openChatRoom(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                                         @PathVariable Long receiverId) {
        return chatService.createChatRoom(authenticatedMember.getId(), receiverId);
    }

    @GetMapping("/chatRoom/auth/{chatRoomId}")
    public void enterChatRoom(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                              @PathVariable Long chatRoomId) {
        chatService.checkIfRoomMember(authenticatedMember.getId(), chatRoomId);
    }

    @MessageMapping("/chats/{chatRoomId}")
    @SendTo("/sub/chatRoom/{chatRoomId}")
    public ChatMessageResponse sendMessage(Long memberId, @DestinationVariable Long chatRoomId, String textContent) {
        return chatService.createChatMessage(memberId, chatRoomId, textContent);
    }

    @GetMapping("/chats/{chatRoomId}")
    public List<ChatMessageResponse> getChatMessages(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                                                     @PathVariable Long chatRoomId,
                                                     @RequestParam(required = false, defaultValue = "2100-12-31T00:00:00")
                                                     LocalDateTime orderThan) {
        return chatService.getPreviousChatMessages(authenticatedMember.getId(), chatRoomId, orderThan);
    }
}
