package com.stoury.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class ChatMessageSaveEvent extends ApplicationEvent {
    private Long memberId;
    private Long chatRoomId;
    private String textContent;
    private LocalDateTime createdAt;

    @Builder
    public ChatMessageSaveEvent(Object source, Long memberId, Long chatRoomId, String textContent, LocalDateTime createdAt) {
        super(source);
        this.memberId = memberId;
        this.chatRoomId = chatRoomId;
        this.textContent = textContent;
        this.createdAt = createdAt;
    }
}
