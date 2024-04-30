package com.stoury.controller;

import com.stoury.dto.SimpleMemberResponse;
import com.stoury.dto.member.AuthenticatedMember;
import com.stoury.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;

    @PostMapping("/follow")
    public void follow(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                       @RequestBody(required = true) String followeeEmail) {
        followService.follow(authenticatedMember.getId(), followeeEmail);
    }

    @GetMapping("/following")
    public List<SimpleMemberResponse> showFollowingMembers(@AuthenticationPrincipal AuthenticatedMember authenticatedMember) {
        return followService.getFollowingMembers(authenticatedMember.getId());
    }

    @GetMapping("/followers")
    public List<SimpleMemberResponse> showFollowersOfMember(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                                                            @RequestParam(required = false, defaultValue = "") String offsetUsername) {
        return followService.getFollowersOfMember(authenticatedMember.getId(), offsetUsername);
    }
}
