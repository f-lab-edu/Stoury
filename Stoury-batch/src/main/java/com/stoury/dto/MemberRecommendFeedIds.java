package com.stoury.dto;

import com.stoury.domain.RecommendFeed;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public record MemberRecommendFeedIds(long memberId, Set<Long> feedIds) {
    public static MemberRecommendFeedIds of(long memberId, Collection<Long> feedIds) {
        return new MemberRecommendFeedIds(memberId, new HashSet<>(feedIds));
    }

    public Stream<RecommendFeed> feedIdsToRecommendFeeds(){
        LocalDateTime currentTime = LocalDateTime.now();

        return feedIds.stream().map(feedId -> new RecommendFeed(memberId, feedId, currentTime));
    }
}
