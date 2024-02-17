package com.stoury.controller;

import com.stoury.dto.chat.ChatMessageResponse;
import com.stoury.dto.chat.ChatRoomResponse;
import com.stoury.dto.member.AuthenticatedMember;
import com.stoury.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    @GetMapping("/chats/{chatRoomId}")
    public List<ChatMessageResponse> getChatMessages(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                                                     @PathVariable Long chatRoomId,
                                                     @RequestParam(required = false, defaultValue = "2100-12-31T00:00:00")
                                                     LocalDateTime orderThan) {
        return chatService.getPreviousChatMessages(authenticatedMember.getId(), chatRoomId, orderThan);
    }

    @PostMapping(value = "/chats/{chatRoomId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendChatMessage(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                                      @PathVariable Long chatRoomId, String textContent) {
        return chatService.sendMessage(authenticatedMember.getId(), chatRoomId, textContent);
    }
}
