package com.stoury.dto.feed;

import java.util.List;
import java.util.Set;

public record FeedUpdateRequest(String textContent, List<String> tagNames, Set<Integer> deleteGraphicContentSequence) {
}
