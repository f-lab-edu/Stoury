package com.stoury.controller;

import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import com.stoury.dto.ErrorResponse;
import com.stoury.dto.feed.FeedCreateRequest;
import com.stoury.dto.feed.FeedResponse;
import com.stoury.dto.feed.FeedUpdateRequest;
import com.stoury.exception.feed.FeedCreateException;
import com.stoury.exception.feed.FeedSearchException;
import com.stoury.exception.feed.FeedUpdateException;
import com.stoury.exception.member.MemberSearchException;
import com.stoury.service.FeedService;
import com.stoury.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class FeedController {
    public static final LocalDateTime DEFAULT_ORDER_THAN = LocalDateTime.of(2100, 12, 31, 0, 0);
    private final MemberService memberService;
    private final FeedService feedService;

    @PostMapping("/feeds")
    public ResponseEntity<FeedResponse> createFeed(@AuthenticationPrincipal User user,
                                                   @RequestPart(name = "feedCreateRequest") FeedCreateRequest feedCreateRequest,
                                                   @RequestPart(required = true) List<MultipartFile> images) {

        Member writer = memberService.getMemberByEmail(user.getUsername());

        FeedResponse createdFeed = feedService.createFeed(writer, feedCreateRequest, images);

        return ResponseEntity.ok(createdFeed);
    }

    @GetMapping("/feeds")
    public ResponseEntity getFeedsOfMember(@RequestParam(required = false) Long memberId,
                                           @RequestParam(required = false) String tagName,
                                           @RequestParam(required = false) Optional<LocalDateTime> orderThan) {
        boolean searchByMember = memberId != null;
        boolean searchByTag = tagName != null;

        List<FeedResponse> feeds;
        if (searchByMember == searchByTag) {
            return ResponseEntity.status(400).body(ErrorResponse.of("Search by a member or a tag"));
        }
        if (searchByMember) {
            feeds = feedService.getFeedsOfMemberId(memberId, orderThan.orElse(DEFAULT_ORDER_THAN));
        } else {
            feeds = feedService.getFeedsByTag(tagName, orderThan.orElse(DEFAULT_ORDER_THAN));
        }

        return ResponseEntity.ok(feeds);
    }

    @PutMapping("/feeds/{feedId}")
    public ResponseEntity updateFeed(@AuthenticationPrincipal User user,
                                     @PathVariable Long feedId,
                                     @RequestBody FeedUpdateRequest feedUpdateRequest) {
        Member writer = memberService.getMemberByEmail(user.getUsername());

        FeedResponse updatedFeed = feedService.updateFeed(feedId, writer, feedUpdateRequest);

        return ResponseEntity.ok(updatedFeed);
    }

    @DeleteMapping("/feeds/{feedId}")
    public ResponseEntity deleteFeed(@AuthenticationPrincipal User user,
                                     @PathVariable Long feedId) {
        Member writer = memberService.getMemberByEmail(user.getUsername());

        Feed feed = feedService.getFeed(feedId);

        if (!feed.getMember().equals(writer)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        feedService.deleteFeed(feed);

        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(value = {FeedCreateException.class, FeedUpdateException.class})
    public ResponseEntity handle400(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(ex.getMessage()));
    }

    @ExceptionHandler(value = {FeedSearchException.class, MemberSearchException.class})
    public ResponseEntity handle404(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(ex.getMessage()));
    }
}
