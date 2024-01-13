package com.stoury.dto;

import com.stoury.domain.Member;
import lombok.Builder;

@Builder
public record MemberCreateRequest(String email, String password, String username, String introduction) {
    public Member toEntity(String encryptedPassword) {
        return Member.builder()
                .email(email)
                .username(username)
                .encryptedPassword(encryptedPassword)
                .introduction(introduction)
                .build();
    }
}
