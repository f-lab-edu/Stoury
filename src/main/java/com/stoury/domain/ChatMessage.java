package com.stoury.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "CHAT_MESSAGE")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = true)
    @JoinColumn(name = "SENDER_ID")
    private Member sender;

    @ManyToOne(optional = false)
    @JoinColumn(name = "CHAT_ROOM_ID")
    private ChatRoom chatRoom;

    @Column(name = "TEXT_CONTENT", columnDefinition = "text", nullable = false)
    private String textContent;

    @Column(name = "CREATED_AT", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;

    public ChatMessage(Member sender, ChatRoom chatRoom, String textContent, LocalDateTime createdAt) {
        this.sender = sender;
        this.chatRoom = chatRoom;
        this.textContent = textContent;
        this.createdAt = createdAt;
    }
}
