package com.stoury.dto.diary;

import com.stoury.domain.Diary;

import java.time.LocalDateTime;

public record SimpleDiaryResponse(Long id, String thumbnail, String title, Long memberId, LocalDateTime createdAt) {
    public static SimpleDiaryResponse from(Diary diary) {
        return new SimpleDiaryResponse(diary.getId(), diary.getThumbnail().getPath(),
                diary.getTitle(), diary.getMember().getId(), diary.getCreatedAt());
    }
}
