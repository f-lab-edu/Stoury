package com.stoury.dto;

import com.stoury.domain.Member;

import java.util.Collection;
import java.util.List;

public record SimpleMemberResponse(Long id, String username) {
    public static SimpleMemberResponse from(Member member) {
        return new SimpleMemberResponse(member.getId(), member.getUsername());
    }

    public static List<SimpleMemberResponse> from(Collection<Member> members) {
        return members.stream().map(SimpleMemberResponse::from).toList();
    }
}
