package com.stoury.dto.member;

import com.stoury.domain.Member;

public record OnlineMember(Long memberId, String username, String email, int distance) {
    public static OnlineMember from(Member member, int distance) {
        return new OnlineMember(member.getId(), member.getUsername(), member.getEmail(), distance);
    }
}
