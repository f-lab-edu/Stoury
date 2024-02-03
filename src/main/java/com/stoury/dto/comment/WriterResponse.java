package com.stoury.dto.comment;

import com.stoury.domain.Member;

public record WriterResponse(Long id, String username) {
    public static WriterResponse from(Member member) {
        return new WriterResponse(member.getId(), member.getUsername());
    }
}
