package com.stoury.dto.chat;

import com.stoury.domain.Member;

public record SenderResponse(Long id, String username, String profileImagePath) {

    public static SenderResponse from(Member sender) {
        return new SenderResponse(
                sender.getId(),
                sender.getUsername(),
                sender.getProfileImagePath()
        );
    }
}
