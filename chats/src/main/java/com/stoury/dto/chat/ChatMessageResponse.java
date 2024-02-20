package com.stoury.dto.chat;

import com.stoury.domain.ChatMessage;
import com.stoury.domain.Member;
import com.stoury.event.ChatMessageSaveEvent;

import java.time.LocalDateTime;

public record ChatMessageResponse(Long id, Long chatRoomId, SenderResponse sender, String textContent,
                                  LocalDateTime createdAt) {

    public static ChatMessageResponse from(ChatMessage chatMessage) {
        return new ChatMessageResponse(
                chatMessage.getId(),
                chatMessage.getChatRoom().getId(),
                SenderResponse.from(chatMessage.getSender()),
                chatMessage.getTextContent(),
                chatMessage.getCreatedAt());
    }

    public static ChatMessageResponse from(ChatMessageSaveEvent chatMessage, Member sender) {
        return new ChatMessageResponse(
                null,
                chatMessage.getChatRoomId(),
                SenderResponse.from(sender),
                chatMessage.getTextContent(),
                chatMessage.getCreatedAt());
    }
}
