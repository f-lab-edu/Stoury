package com.stoury.dto;

import java.util.List;

public record FrequentTags(Long memberId, List<String> viewedTags) {
}
