package com.stoury.dto.diary;

import com.stoury.domain.Diary;

public record SimpleDiaryResponse(Long id, String thumbnail, String title, Long memberId) {
    public static SimpleDiaryResponse from(Diary diary) {
        return new SimpleDiaryResponse(diary.getId(), diary.getThumbnail().getPath(),
                diary.getTitle(), diary.getMember().getId());
    }
}
