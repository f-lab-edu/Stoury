package com.stoury.dto;

import com.stoury.domain.Feed;
import com.stoury.domain.GraphicContent;
import com.stoury.domain.Member;
import com.stoury.domain.Tag;
import com.stoury.exception.feed.FeedCreateException;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public record FeedCreateRequest(String textContent, Double latitude, Double longitude, List<String> tagNames) {
    public FeedCreateRequest{
        if (tagNames == null) {
            tagNames = new ArrayList<>();
        }
        if (latitude == null) {
            throw new FeedCreateException("Latitude is required");
        }
        if (longitude == null) {
            throw new FeedCreateException("Longitude is required");
        }
    }
    public Feed toEntity(Member writer, List<GraphicContent> graphicContents, List<Tag> tags) {
        Feed feed = Feed.builder()
                .member(writer)
                .textContent(textContent)
                .latitude(latitude)
                .longitude(longitude)
                .tags(tags)
                .build();
        feed.addGraphicContents(graphicContents);

        return feed;
    }
}
