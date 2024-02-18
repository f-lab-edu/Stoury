package com.stoury.repository;

import com.stoury.domain.ChatMessage;
import com.stoury.domain.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllByChatRoomAndCreatedAtBefore(ChatRoom chatRoom, LocalDateTime createdAt, Pageable pageable);
}
