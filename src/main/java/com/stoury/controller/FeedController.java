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

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FeedController {
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
}
