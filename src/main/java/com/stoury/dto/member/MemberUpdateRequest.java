package com.stoury.dto.member;

import lombok.Builder;

@Builder
public record MemberUpdateRequest(Long id, String email, String username, String introduction) {
}
