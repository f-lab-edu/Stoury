package com.stoury.controller;

import com.stoury.dto.feed.FeedCreateRequest;
import com.stoury.dto.feed.FeedResponse;
import com.stoury.dto.feed.FeedUpdateRequest;
import com.stoury.dto.member.AuthenticatedMember;
import com.stoury.service.FeedService;
import com.stoury.utils.Values;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FeedController {
    private final FeedService feedService;

    @PostMapping("/feeds")
    public FeedResponse createFeed(@AuthenticationPrincipal AuthenticatedMember member,
                                   @RequestPart(name = "feedCreateRequest") FeedCreateRequest feedCreateRequest,
                                   @RequestPart(required = true) List<MultipartFile> images) {
        return feedService.createFeed(member.getId(), feedCreateRequest, images);
    }

    @GetMapping("/feeds/{feedId}")
    public FeedResponse getFeed(@PathVariable Long feedId) {
        return feedService.getFeed(feedId);
    }

    @GetMapping("/feeds/tag/{tagName}")
    public List<FeedResponse> getFeedsOfTag(@PathVariable String tagName,
                                            @RequestParam(required = false, defaultValue = Values.MAX_LONG) Long orderThan) {
        return feedService.getFeedsByTag(tagName, orderThan);
    }

    @GetMapping("/feeds/member/{memberId}")
    public List<FeedResponse> getFeedsOfMember(@PathVariable Long memberId,
                                               @RequestParam(required = false, defaultValue = Values.MAX_LONG) Long cursorId) {
        return feedService.getFeedsOfMemberId(memberId, cursorId);
    }

    @PutMapping("/feeds/{feedId}")
    public FeedResponse updateFeed(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                                   @PathVariable Long feedId,
                                   @RequestBody FeedUpdateRequest feedUpdateRequest) {
        return feedService.updateFeedIfOwner(feedId, feedUpdateRequest, authenticatedMember.getId());
    }

    @DeleteMapping("/feeds/{feedId}")
    public void deleteFeed(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                           @PathVariable Long feedId) {
        feedService.deleteFeedIfOwner(feedId, authenticatedMember.getId());
    }
}
