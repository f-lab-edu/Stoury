package com.stoury.service.kafka;

import com.stoury.config.sse.SseEmitters;
import com.stoury.dto.chat.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {
    private final SseEmitters sseEmitters;

    @KafkaListener(topics = "chats", containerGroup = "${spring.kafka.consumer.group-id}")
    public void broadcast(@Payload ChatMessageResponse chatMessage) {
        sseEmitters.broadcast(chatMessage.chatRoomId(), chatMessage);
    }
}
