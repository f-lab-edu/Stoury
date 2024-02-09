package com.stoury.dto.diary;

import com.stoury.domain.Diary;
import org.springframework.data.domain.Page;

import java.util.List;

public record DiaryPageResponse(List<SimpleDiaryResponse> diaries, int pageNo, boolean hasNext) {
    public static DiaryPageResponse from(Page<Diary> page) {
        List<SimpleDiaryResponse> diaries = page.getContent().stream().map(SimpleDiaryResponse::from).toList();
        int pageNo = page.getNumber();
        boolean hasNext = page.hasNext();

        return new DiaryPageResponse(diaries, pageNo, hasNext);
    }
}
