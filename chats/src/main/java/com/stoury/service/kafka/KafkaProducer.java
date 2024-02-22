package com.stoury.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stoury.exception.chat.ChatMessageSendException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    public static final String TOPIC_NAME="chats";

    public <T> void produce(T message) {
        String rawMessageJson = convertToString(message);
        kafkaTemplate.send(TOPIC_NAME, rawMessageJson);
    }

    private <T> String convertToString(T message) {
        String rawMessageJson = null;
        try {
            rawMessageJson = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new ChatMessageSendException(e);
        }
        return rawMessageJson;
    }
}
