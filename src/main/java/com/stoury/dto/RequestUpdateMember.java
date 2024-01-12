package com.stoury.dto;

import lombok.Builder;

@Builder
public record RequestUpdateMember(String email, String username, String profileImagePath, String introduction) {
}
