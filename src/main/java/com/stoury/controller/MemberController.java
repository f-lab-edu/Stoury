package com.stoury.controller;

import com.stoury.dto.member.AuthenticatedMember;
import com.stoury.dto.member.MemberCreateRequest;
import com.stoury.dto.member.MemberResponse;
import com.stoury.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/members")
    public MemberResponse createMember(@RequestBody MemberCreateRequest memberCreateRequest) {
        return memberService.createMember(memberCreateRequest);
}

    @PostMapping("/members/set-online")
    public void setOnline(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                          @RequestParam(required = false) Double latitude,
                          @RequestParam(required = false) Double longitude) {
        memberService.setOnline(authenticatedMember.getId(), latitude, longitude);
    }

    @PostMapping("/members/set-offline")
    public void setOffline(@AuthenticationPrincipal AuthenticatedMember authenticatedMember) {
        memberService.setOffline(authenticatedMember.getId());
    }
}
