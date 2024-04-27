package com.stoury.dto;

import java.util.Set;

public record MemberRecommendFeedIds(long memberId, Set<Long> feedIds) {
}
