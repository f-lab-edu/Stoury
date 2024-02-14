package com.stoury.dto;

import com.stoury.domain.Member;

public record SimpleMemberResponse(Long id, String username) {
    public static SimpleMemberResponse from(Member member) {
        return new SimpleMemberResponse(member.getId(), member.getUsername());
    }
}
