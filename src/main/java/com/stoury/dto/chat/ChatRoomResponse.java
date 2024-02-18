package com.stoury.dto.chat;

import com.stoury.domain.ChatRoom;
import com.stoury.dto.SimpleMemberResponse;

import java.util.List;

public record ChatRoomResponse(Long id, List<SimpleMemberResponse> members) {
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getMembers().stream().map(SimpleMemberResponse::from).toList()
        );
    }
}
