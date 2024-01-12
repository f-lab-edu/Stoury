package com.stoury.dto;

import lombok.Builder;

@Builder
public record RequestMember(String email, String password, String username, String introduction) {
}
