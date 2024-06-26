package com.stoury.dto.feed;

import com.stoury.domain.Feed;
import com.stoury.domain.GraphicContent;
import com.stoury.domain.Member;
import com.stoury.domain.Tag;
import com.stoury.exception.feed.FeedCreateException;
import lombok.Builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
public record FeedCreateRequest(String textContent, Double latitude, Double longitude, Set<String> tagNames) {
    public FeedCreateRequest{
        if (tagNames == null) {
            tagNames = new HashSet<>();
        }
        if (latitude == null) {
            throw new FeedCreateException("Latitude is required");
        }
        if (longitude == null) {
            throw new FeedCreateException("Longitude is required");
        }
    }
    public Feed toEntity(Member writer, List<GraphicContent> graphicContents, Set<Tag> tags, String city, String country) {
        Feed feed = Feed.builder()
                .member(writer)
                .textContent(textContent)
                .latitude(latitude)
                .longitude(longitude)
                .tags(tags)
                .city(city)
                .country(country)
                .build();
        feed.addGraphicContents(graphicContents);

        return feed;
    }
}
