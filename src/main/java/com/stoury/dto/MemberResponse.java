package com.stoury.dto;

import com.stoury.domain.Member;
import lombok.Builder;

@Builder
public record MemberResponse(String email, String username, String profileImagePath) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getEmail(),
                member.getUsername(),
                member.getProfileImagePath());
    }
}
