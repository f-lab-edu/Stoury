package com.stoury.controller;

import com.stoury.dto.member.AuthenticatedMember;
import com.stoury.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/like/feed/{feedId}")
    public boolean checkWhetherLiked(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                                     @PathVariable Long feedId){
        return likeService.checkWhetherLiked(authenticatedMember.getId(), feedId);
    }
}
