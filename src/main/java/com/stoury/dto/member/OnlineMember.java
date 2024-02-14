package com.stoury.dto.member;

import jakarta.validation.constraints.NotNull;

public record OnlineMember(@NotNull Long memberId, Double latitude, Double longitude) {
}
