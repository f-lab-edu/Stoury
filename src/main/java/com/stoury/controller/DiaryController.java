package com.stoury.controller;

import com.stoury.dto.diary.DiaryCreateRequest;
import com.stoury.dto.diary.DiaryPageResponse;
import com.stoury.dto.diary.DiaryResponse;
import com.stoury.dto.member.AuthenticatedMember;
import com.stoury.service.DiaryService;
import com.stoury.utils.Values;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/diaries/{diaryId}")
    public DiaryResponse getDiary(@PathVariable Long diaryId) {
        return diaryService.getDiary(diaryId);
    }

    @GetMapping("/diaries/member/{memberId}")
    public DiaryPageResponse getMemberDiaries(@PathVariable Long memberId,
                                              @RequestParam(required = false, defaultValue = Values.MAX_LONG) Long offsetId) {
        return diaryService.getMemberDiaries(memberId, offsetId);
    }

    @DeleteMapping("/diaries/{diaryId}")
    public void cancelDiary(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                            @PathVariable Long diaryId) {
        diaryService.cancelDiaryIfOwner(diaryId, authenticatedMember.getId());
    }
}
