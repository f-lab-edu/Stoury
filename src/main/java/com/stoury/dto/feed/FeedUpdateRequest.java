package com.stoury.dto.feed;

import java.util.List;

public record FeedUpdateRequest(String textContent, Double latitude, Double longitude,
                                List<String> tagNames, List<Integer> deleteGraphicContentSequence) {
}
