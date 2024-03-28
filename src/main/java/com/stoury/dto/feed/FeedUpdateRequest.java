package com.stoury.dto.feed;

import java.util.Set;

public record FeedUpdateRequest(String textContent, Set<String> tagNames, Set<Integer> deleteGraphicContentSequence) {
}
