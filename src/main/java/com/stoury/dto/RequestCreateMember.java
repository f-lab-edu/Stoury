package com.stoury.dto;

import lombok.Builder;

@Builder
public record RequestCreateMember(String email, String password, String username, String introduction) {
}
