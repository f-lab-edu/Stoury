package com.stoury.dto;

import com.stoury.domain.Feed;
import com.stoury.domain.GraphicContent;
import com.stoury.domain.Tag;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.List;

public record FeedResponse(Long feedId,
                           MemberResponse memberResponse,
                           List<String> graphicContentsPaths,
                           String textContent,
                           List<String> tagNames,
                           Double latitude,
                           Double longitude,
                           int likes,
                           LocalDateTime createdAt) {
    public static FeedResponse from(Feed uploadedFeed) {
        return new FeedResponse(
                uploadedFeed.getId(),
                MemberResponse.from(uploadedFeed.getMember()),
                uploadedFeed.getGraphicContents().stream().map(GraphicContent::getPath).toList(),
                uploadedFeed.getTextContent(),
                uploadedFeed.getTags().stream().map(Tag::getTagName).toList(),
                uploadedFeed.getLatitude(),
                uploadedFeed.getLongitude(),
                uploadedFeed.getLikes(),
                uploadedFeed.getCreatedAt()
        );
    }

    public static Slice<FeedResponse> from(Slice<Feed> feedSlice) {
        List<FeedResponse> feedResponses = feedSlice.stream()
                .map(FeedResponse::from)
                .toList();

        return new SliceImpl<>(feedResponses, feedSlice.getPageable(), feedSlice.hasNext());
    }
}
