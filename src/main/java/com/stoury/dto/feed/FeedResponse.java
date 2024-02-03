package com.stoury.dto.feed;

import com.stoury.domain.Feed;
import com.stoury.domain.GraphicContent;
import com.stoury.domain.Tag;
import com.stoury.dto.member.MemberResponse;

import java.time.LocalDateTime;
import java.util.List;

public record FeedResponse(Long feedId,
                           MemberResponse memberResponse,
                           List<String> graphicContentsPaths,
                           String textContent,
                           Double latitude,
                           Double lonitude,
                           List<String> tagNames,
                           long likes,
                           LocalDateTime createdAt) {
    public static FeedResponse from(Feed feed, long feedLikes) {
        return new FeedResponse(
                feed.getId(),
                MemberResponse.from(feed.getMember()),
                feed.getGraphicContents().stream().map(GraphicContent::getPath).toList(),
                feed.getTextContent(),
                feed.getLatitude(),
                feed.getLongitude(),
                feed.getTags().stream().map(Tag::getTagName).toList(),
                feedLikes,
                feed.getCreatedAt()
        );
    }
}
