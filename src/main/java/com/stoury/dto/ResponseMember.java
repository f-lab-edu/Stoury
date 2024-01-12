package com.stoury.dto;

import com.stoury.domain.Member;
import lombok.Builder;

@Builder
public record ResponseMember(String email, String username, String profileImagePath) {
    public static ResponseMember from(Member member) {
        return new ResponseMember(
                member.getEmail(),
                member.getUsername(),
                member.getProfileImagePath());
    }
}
