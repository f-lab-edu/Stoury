package com.stoury.controller;

import com.stoury.dto.diary.DiaryCreateRequest;
import com.stoury.dto.diary.DiaryResponse;
import com.stoury.dto.member.AuthenticatedMember;
import com.stoury.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;

    @PostMapping("/diaries")
    public DiaryResponse createDiary(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                                     @RequestBody(required = true) DiaryCreateRequest diaryCreateRequest) {
        return diaryService.createDiary(diaryCreateRequest, authenticatedMember.getId());
    }
}
