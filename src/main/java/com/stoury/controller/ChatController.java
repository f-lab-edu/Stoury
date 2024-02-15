package com.stoury.controller;

import com.stoury.dto.chat.ChatMessageResponse;
import com.stoury.dto.chat.ChatRoomResponse;
import com.stoury.dto.member.AuthenticatedMember;
import com.stoury.service.ChatService;
import com.stoury.utils.JwtUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
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
    private final JwtUtils jwtUtils;

    @PostMapping("/chats/to/{receiverId}")
    public ChatRoomResponse openChatRoom(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                                         HttpServletResponse response,
                                         @PathVariable Long receiverId) {
        Long memberId = authenticatedMember.getId();
        ChatRoomResponse chatRoom = chatService.createChatRoom(memberId, receiverId);
        response.setHeader("Authorization", jwtUtils.issueToken(memberId));
        return chatRoom;
    }

    @MessageMapping("/chats/{chatRoomId}")
    @SendTo("/sub/chatRoom/{chatRoomId}")
    public ChatMessageResponse sendMessage(@Header(value = "Authorization", required = true) String authToken,
                                           @DestinationVariable Long chatRoomId,
                                           String textContent) {
        Long memberId = jwtUtils.getMemberId(authToken);
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
