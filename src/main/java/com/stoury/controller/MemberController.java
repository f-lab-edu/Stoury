package com.stoury.controller;

import com.stoury.dto.MemberCreateRequest;
import com.stoury.dto.MemberResponse;
import com.stoury.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/members")
    public ResponseEntity<MemberResponse> createMember(@RequestBody MemberCreateRequest memberCreateRequest) {
        MemberResponse createdMember = memberService.createMember(memberCreateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdMember);
    }
}
