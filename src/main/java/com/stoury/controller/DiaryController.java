package com.stoury.controller;

import com.stoury.dto.diary.DiaryCreateRequest;
import com.stoury.dto.diary.DiaryResponse;
import com.stoury.dto.diary.SimpleDiaryResponse;
import com.stoury.dto.member.AuthenticatedMember;
import com.stoury.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;

    @PostMapping("/diaries")
    public DiaryResponse createDiary(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                                     @RequestBody(required = true) DiaryCreateRequest diaryCreateRequest) {
        return diaryService.createDiary(diaryCreateRequest, authenticatedMember.getId());
    }

    @GetMapping("/diaries/member/{memberId}")
    public Page<SimpleDiaryResponse> getMemberDiaries(@PathVariable Long memberId,
                                                      @RequestParam(required = false, defaultValue = "0") int pageNo) {
        return diaryService.getMemberDiaries(memberId, pageNo);
    }

    @DeleteMapping("/diaries/{diaryId}")
    public void cancelDiary(@PathVariable Long diaryId) {
        diaryService.cancelDiary(diaryId);
    }
}
