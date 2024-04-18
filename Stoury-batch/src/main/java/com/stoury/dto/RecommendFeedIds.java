package com.stoury.dto;

import java.util.List;

public record RecommendFeedIds(Long memberId, List<Long> feedIds) {
}
