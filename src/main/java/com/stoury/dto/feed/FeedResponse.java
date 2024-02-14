package com.stoury.dto.feed;

import com.stoury.domain.Feed;
import com.stoury.domain.GraphicContent;
import com.stoury.domain.Tag;
import com.stoury.dto.SimpleMemberResponse;

import java.time.LocalDateTime;
import java.util.List;

public record FeedResponse(Long feedId,
                           SimpleMemberResponse writer,
                           List<String> graphicContentsPaths,
                           String textContent,
                           Double latitude,
                           Double lonitude,
                           List<String> tagNames,
                           LocationResponse location,
                           long likes,
                           LocalDateTime createdAt) {
    public static FeedResponse from(Feed feed, long feedLikes) {
        return new FeedResponse(
                feed.getId(),
                SimpleMemberResponse.from(feed.getMember()),
                feed.getGraphicContents().stream().map(GraphicContent::getPath).toList(),
                feed.getTextContent(),
                feed.getLatitude(),
                feed.getLongitude(),
                feed.getTags().stream().map(Tag::getTagName).toList(),
                new LocationResponse(feed.getCity(), feed.getCountry()),
                feedLikes,
                feed.getCreatedAt()
        );
    }
}
