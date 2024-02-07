package com.stoury.controller;

import com.stoury.dto.member.AuthenticatedMember;
import com.stoury.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Object> likeFeed(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                                           @PathVariable Long feedId) {
        likeService.like(authenticatedMember.getId(), feedId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/like/feed/{feedId}")
    public ResponseEntity<Object> cancelLikeFeed(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                                                 @PathVariable Long feedId) {
        likeService.likeCancel(authenticatedMember.getId(), feedId);

        return ResponseEntity.ok().build();
    }
}
