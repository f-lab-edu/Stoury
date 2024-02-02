package com.stoury.controller;

import com.stoury.dto.feed.FeedCreateRequest;
import com.stoury.dto.feed.FeedResponse;
import com.stoury.dto.feed.FeedUpdateRequest;
import com.stoury.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class FeedController {
    private final FeedService feedService;

    @PostMapping("/feeds")
    public ResponseEntity<FeedResponse> createFeed(@AuthenticationPrincipal User user,
                                                   @RequestPart(name = "feedCreateRequest") FeedCreateRequest feedCreateRequest,
                                                   @RequestPart(required = true) List<MultipartFile> images) {
        FeedResponse createdFeed = feedService.createFeed(user.getUsername(), feedCreateRequest, images);
        return ResponseEntity.ok(createdFeed);
    }

    @GetMapping("/feeds/tag/{tagName}")
    public ResponseEntity<Object> getFeedsOfTag(@PathVariable String tagName,
                                                @RequestParam(required = false, defaultValue = "2100-12-31T00:00:00")
                                                LocalDateTime orderThan) {
        List<FeedResponse> feeds = feedService.getFeedsByTag(tagName, orderThan);
        return ResponseEntity.ok(feeds);
    }

    @GetMapping("/feeds/member/{memberId}")
    public ResponseEntity<Object> getFeedsOfMember(@PathVariable Long memberId,
                                                   @RequestParam(required = false, defaultValue = "2100-12-31T00:00:00")
                                                   LocalDateTime orderThan) {
        List<FeedResponse> feeds = feedService.getFeedsOfMemberId(memberId, orderThan);
        return ResponseEntity.ok(feeds);
    }

    @PutMapping("/feeds/{feedId}")
    public ResponseEntity<FeedResponse> updateFeed(@AuthenticationPrincipal User user,
                                                   @PathVariable Long feedId,
                                                   @RequestBody FeedUpdateRequest feedUpdateRequest) {
        FeedResponse updatedFeed = feedService.updateFeed(feedId, user.getUsername(), feedUpdateRequest);
        return ResponseEntity.ok(updatedFeed);
    }

    @DeleteMapping("/feeds/{feedId}")
    public ResponseEntity<Object> deleteFeed(@AuthenticationPrincipal User user, @PathVariable Long feedId) {
        feedService.deleteFeed(feedId, user.getUsername());
        return ResponseEntity.ok().build();
    }
}
