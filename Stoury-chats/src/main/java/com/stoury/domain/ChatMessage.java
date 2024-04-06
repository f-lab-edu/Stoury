package com.stoury.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@DynamicInsert
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

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    public ChatMessage(Member sender, ChatRoom chatRoom, String textContent) {
        this.sender = sender;
        this.chatRoom = chatRoom;
        this.textContent = textContent;
    }

    public ChatMessage(Member sender, ChatRoom chatRoom, String textContent, LocalDateTime createdAt) {
        this(sender, chatRoom, textContent);
        this.createdAt = createdAt;
    }
}
