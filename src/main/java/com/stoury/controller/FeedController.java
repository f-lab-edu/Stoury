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
    public FeedResponse createFeed(@AuthenticationPrincipal User user,
                                                   @RequestPart(name = "feedCreateRequest") FeedCreateRequest feedCreateRequest,
                                                   @RequestPart(required = true) List<MultipartFile> images) {
        return feedService.createFeed(user.getUsername(), feedCreateRequest, images);
    }

    @GetMapping("/feeds/tag/{tagName}")
    public List<FeedResponse> getFeedsOfTag(@PathVariable String tagName,
                                            @RequestParam(required = false, defaultValue = "2100-12-31T00:00:00")
                                                LocalDateTime orderThan) {
        return feedService.getFeedsByTag(tagName, orderThan);
    }

    @GetMapping("/feeds/member/{memberId}")
    public List<FeedResponse> getFeedsOfMember(@PathVariable Long memberId,
                                                   @RequestParam(required = false, defaultValue = "2100-12-31T00:00:00")
                                                   LocalDateTime orderThan) {
        return feedService.getFeedsOfMemberId(memberId, orderThan);
    }

    @GetMapping("/feeds/popular/abroad-spots")
    public List<String> getPopularAbroadSpots() {
        return feedService.getPopularAbroadSpots();
    }

    @GetMapping("/feeds/popular/domestic-spots")
    public List<String> getPopularDomesticSpots() {
        return feedService.getPopularDomesticSpots();
    }

    @PutMapping("/feeds/{feedId}")
    public FeedResponse updateFeed(@AuthenticationPrincipal User user,
                                                   @PathVariable Long feedId,
                                                   @RequestBody FeedUpdateRequest feedUpdateRequest) {
        return feedService.updateFeed(feedId, user.getUsername(), feedUpdateRequest);
    }

    @DeleteMapping("/feeds/{feedId}")
    public ResponseEntity<Object> deleteFeed(@AuthenticationPrincipal User user, @PathVariable Long feedId) {
        feedService.deleteFeed(feedId, user.getUsername());
        return ResponseEntity.ok().build();
    }
}
