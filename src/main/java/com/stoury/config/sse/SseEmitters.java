package com.stoury.config.sse;

import com.stoury.dto.chat.ChatMessageResponse;
import com.stoury.exception.chat.ChatMessageSendException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SseEmitters {
    public static final long TIMEOUT = 60 * 1000L;
    public static final String ROOM_ID_NULL_MESSAGE = "Room id cannot be null";
    private static final Map<Long, SseEmitter> chatRoomEmitters = new ConcurrentHashMap<>();

    public SseEmitter get(Long roomId) {
        Long roomIdNotNull = Objects.requireNonNull(roomId, ROOM_ID_NULL_MESSAGE);

        return chatRoomEmitters.computeIfAbsent(roomIdNotNull, key -> chatRoomEmitter(roomIdNotNull));
    }

    @NotNull
    private SseEmitter chatRoomEmitter(Long roomId) {
        Long roomIdNotNull = Objects.requireNonNull(roomId, ROOM_ID_NULL_MESSAGE);

        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitter.onCompletion(() -> chatRoomEmitters.remove(roomIdNotNull, emitter));
        emitter.onTimeout(emitter::complete);
        return emitter;
    }

    public void broadCast(Long roomId, ChatMessageResponse chatMessage) {
        Long roomIdNotNull = Objects.requireNonNull(roomId, ROOM_ID_NULL_MESSAGE);

        log.info("Trying to connect to {}", roomId);
        SseEmitter emitter = get(roomIdNotNull);
        try {
            emitter.send(SseEmitter.event()
                    .name("ChatMessage")
                    .data(chatMessage));
        } catch (IOException e) {
            throw new ChatMessageSendException(e);
        }
        log.info("Established");
    }
}