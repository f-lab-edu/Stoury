package com.stoury.service.kafka;

import com.stoury.utils.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;
    public static final String TOPIC_NAME="chats";

    public <T> void produce(T message) {
        String rawMessageJson = jsonMapper.getJsonString(message);
        kafkaTemplate.send(TOPIC_NAME, rawMessageJson);
    }
}
