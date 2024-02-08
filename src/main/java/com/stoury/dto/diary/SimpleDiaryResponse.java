package com.stoury.dto.diary;

import com.stoury.domain.Diary;

public record SimpleDiaryResponse(Long id, String thumbnail, String title) {
    public static SimpleDiaryResponse from(Diary diary) {
        return new SimpleDiaryResponse(diary.getId(), diary.getThumbnailPath(), diary.getTitle());
    }
}
