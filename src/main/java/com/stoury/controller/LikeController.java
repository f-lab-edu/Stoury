package com.stoury.controller;

import com.stoury.dto.member.AuthenticatedMember;
import com.stoury.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/like/feed/{feedId}")
    public void likeFeed(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                         @PathVariable Long feedId) {
        likeService.like(authenticatedMember.getId(), feedId);
    }

    @DeleteMapping("/like/feed/{feedId}")
    public void cancelLikeFeed(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                               @PathVariable Long feedId) {
        likeService.likeCancel(authenticatedMember.getId(), feedId);
    }
}
