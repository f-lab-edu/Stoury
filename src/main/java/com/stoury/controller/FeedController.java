package com.stoury.controller;

import com.stoury.domain.Member;
import com.stoury.dto.FeedCreateRequest;
import com.stoury.dto.FeedResponse;
import com.stoury.service.FeedService;
import com.stoury.service.MemberService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<List<FeedResponse>> getFeedsOfMember(@RequestParam(required = true) Long memberId,
                                                               @RequestParam(required = false) Optional<LocalDateTime> orderThan) {
        List<FeedResponse> feeds = feedService.getFeedsOfMemberId(memberId, orderThan.orElse(DEFAULT_ORDER_THAN));

        return ResponseEntity.ok(feeds);
    }
}
