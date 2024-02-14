package com.stoury.controller;

import com.stoury.dto.member.AuthenticatedMember;
import com.stoury.dto.member.MemberCreateRequest;
import com.stoury.dto.member.MemberResponse;
import com.stoury.dto.member.OnlineMember;
import com.stoury.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/members/around")
    public List<OnlineMember> getAroundMembers(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                                               @RequestParam(required = true) Double latitude,
                                               @RequestParam(required = true) Double longitude,
                                               @RequestParam(required = false, defaultValue = "10") int radiusKm) {
        return memberService.searchOnlineMembers(authenticatedMember.getId(), latitude, longitude, radiusKm);
    }
}
