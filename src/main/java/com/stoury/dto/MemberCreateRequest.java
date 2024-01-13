package com.stoury.dto;

import lombok.Builder;

@Builder
public record MemberCreateRequest(String email, String password, String username, String introduction) {
}
