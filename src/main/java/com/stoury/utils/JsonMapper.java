package com.stoury.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stoury.dto.diary.SimpleDiaryResponse;
import com.stoury.dto.feed.SimpleFeedResponse;
import com.stoury.exception.JsonConvertException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonMapper {
    private final ObjectMapper objectMapper;

    public String getJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonConvertException(object, e);
        }
    }

    public byte[] getJsonBytes(Object object) {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new JsonConvertException(object, e);
        }
    }

    public <T> T stringJsonToObject(String rawJson, Class<T> clazz) {
        try {
            return objectMapper.readValue(rawJson, clazz);
        } catch (JsonProcessingException e) {
            throw new JsonConvertException(rawJson, e);
        }
    }

    public <T> T stringJsonToObject(String rawJson, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(rawJson, typeReference);
        } catch (JsonProcessingException e) {
            throw new JsonConvertException(rawJson, e);
        }
    }
}
