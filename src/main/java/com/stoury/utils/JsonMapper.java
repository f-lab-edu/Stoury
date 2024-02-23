package com.stoury.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
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

    public SimpleDiaryResponse getDiaryResponse(String rawSimpleDiaryJson) {
        try {
            return objectMapper.readValue(rawSimpleDiaryJson, SimpleDiaryResponse.class);
        } catch (JsonProcessingException e) {
            throw new JsonConvertException(rawSimpleDiaryJson, e);
        }
    }

    public SimpleFeedResponse getFeedResponse(String rawSimpleFeedJson) {
        try {
            return objectMapper.readValue(rawSimpleFeedJson, SimpleFeedResponse.class);
        } catch (JsonProcessingException e) {
            throw new JsonConvertException(rawSimpleFeedJson, e);
        }
    }
}
