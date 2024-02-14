package com.stoury.dto.feed;

import com.stoury.domain.Feed;
import com.stoury.dto.SimpleMemberResponse;

public record SimpleFeedResponse(Long id, SimpleMemberResponse writer, String city, String country) {

    public static SimpleFeedResponse from(Feed feed) {
        return new SimpleFeedResponse(feed.getId(),
                SimpleMemberResponse.from(feed.getMember()),
                feed.getCity(),
                feed.getCountry());
    }
}
