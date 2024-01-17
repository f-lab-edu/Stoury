package com.stoury.dto;

import com.stoury.domain.Feed;
import com.stoury.domain.GraphicContent;

import java.time.LocalDateTime;
import java.util.List;

public record FeedResponse(Long feedId,
                           MemberResponse memberResponse,
                           List<String> graphicContentsPaths,
                           String textContent,
                           Double latitude,
                           Double longitude,
                           LocalDateTime createdAt) {
    public static FeedResponse from(Feed uploadedFeed) {
        return new FeedResponse(
                uploadedFeed.getId(),
                MemberResponse.from(uploadedFeed.getMember()),
                uploadedFeed.getGraphicContents().stream().map(GraphicContent::getPath).toList(),
                uploadedFeed.getTextContent(),
                uploadedFeed.getLatitude(),
                uploadedFeed.getLongitude(),
                uploadedFeed.getCreatedAt()
        );
    }
}