package com.stoury.dto.feed;

import com.stoury.domain.Feed;
import com.stoury.domain.Tag;
import com.stoury.dto.SimpleMemberResponse;
import com.stoury.projection.FeedResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record FeedResponse(Long feedId,
                           SimpleMemberResponse writer,
                           List<GraphicContentResponse> graphicContentsPaths,
                           String textContent,
                           Double latitude,
                           Double longitude,
                           Set<String> tagNames,
                           LocationResponse location,
                           long likes,
                           LocalDateTime createdAt) {
    public static FeedResponse from(Feed feed, long feedLikes) {
        return new FeedResponse(
                feed.getId(),
                SimpleMemberResponse.from(feed.getMember()),
                feed.getGraphicContents().stream().map(GraphicContentResponse::from).toList(),
                feed.getTextContent(),
                feed.getLatitude(),
                feed.getLongitude(),
                feed.getTags().stream().map(Tag::getTagName).collect(Collectors.toSet()),
                new LocationResponse(feed.getCity(), feed.getCountry()),
                feedLikes,
                feed.getCreatedAt()
        );
    }

    public static FeedResponse from(FeedResponseEntity feedResponseEntity,
                                    SimpleMemberResponse writer,
                                    List<GraphicContentResponse> graphicContents,
                                    Set<String> tags,
                                    LocationResponse location,
                                    long likes) {
        return new FeedResponse(
                feedResponseEntity.getFeedId(),
                writer,
                graphicContents,
                feedResponseEntity.getTextContent(),
                feedResponseEntity.getLatitude(),
                feedResponseEntity.getLongitude(),
                tags,
                location,
                likes,
                feedResponseEntity.getCreatedAt());
    }
}
