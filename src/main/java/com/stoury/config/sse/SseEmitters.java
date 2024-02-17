package com.stoury.config.sse;

import com.stoury.dto.chat.ChatMessageResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitters {
    public static final long TIMEOUT = 60 * 1000L;
    private Map<Long, SseEmitter> chatRoomEmitters = new ConcurrentHashMap<>();

    public SseEmitter get(Long roomId){
        Long roomIdNotNull = Objects.requireNonNull(roomId, "Room id cannot be null");

        return chatRoomEmitters.putIfAbsent(roomIdNotNull, chatRoomEmitter(roomIdNotNull));
    }

    @NotNull
    private SseEmitter chatRoomEmitter(Long roomId) {
        Long roomIdNotNull = Objects.requireNonNull(roomId, "Room id cannot be null");

        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitter.onCompletion(() -> chatRoomEmitters.remove(roomIdNotNull, emitter));
        emitter.onTimeout(emitter::complete);
        return emitter;
    }

    public SseEmitter broadCast(Long roomId, ChatMessageResponse chatMessage) {
        Long roomIdNotNull = Objects.requireNonNull(roomId, "Room id cannot be null");

        SseEmitter emitter = get(roomIdNotNull);
        try {
            emitter.send(SseEmitter.event()
                    .name("ChatMessage")
                    .data(chatMessage));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return emitter;
    }
}
