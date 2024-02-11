package com.stoury.dto.diary;

import java.util.List;

public record DiaryCreateRequest(String title, List<Long> feedIds, Long thumbnailId) {
}
