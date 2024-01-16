package com.stoury.dto;

import com.stoury.domain.Feed;
import com.stoury.domain.GraphicContent;
import com.stoury.domain.Member;
import lombok.Builder;

import java.util.List;

@Builder
public record FeedCreateRequest(String textContent, Double latitude, Double longitude) {
    public Feed toEntity(Member writer, List<GraphicContent> graphicContents) {
        Feed feed = Feed.builder()
                .member(writer)
                .textContent(textContent)
                .latitude(latitude)
                .longitude(longitude)
                .build();
        feed.addGraphicContents(graphicContents);

        return feed;
    }
}
