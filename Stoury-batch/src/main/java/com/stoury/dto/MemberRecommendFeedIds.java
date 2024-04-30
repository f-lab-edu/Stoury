package com.stoury.dto;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public record MemberRecommendFeedIds(long memberId, Set<Long> feedIds) {
    public static MemberRecommendFeedIds of(long memberId, Collection<Long> feedIds) {
        return new MemberRecommendFeedIds(memberId, new HashSet<>(feedIds));
    }
}
