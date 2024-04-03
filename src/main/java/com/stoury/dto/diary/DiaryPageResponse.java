package com.stoury.dto.diary;

import com.stoury.domain.Diary;
import org.springframework.data.domain.Page;

import java.util.List;

public record DiaryPageResponse(List<SimpleDiaryResponse> diaries) {
    public static DiaryPageResponse from(List<Diary> page) {
        List<SimpleDiaryResponse> diaries = page.stream().map(SimpleDiaryResponse::from).toList();

        return new DiaryPageResponse(diaries);
    }
}
