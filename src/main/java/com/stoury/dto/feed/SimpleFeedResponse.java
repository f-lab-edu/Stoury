package com.stoury.dto.feed;

import com.stoury.domain.Feed;
import com.stoury.dto.WriterResponse;

public record SimpleFeedResponse(Long id, WriterResponse writer, String city, String country) {

    public static SimpleFeedResponse from(Feed feed) {
        return new SimpleFeedResponse(feed.getId(),
                WriterResponse.from(feed.getMember()),
                feed.getCity(),
                feed.getCountry());
    }
}
