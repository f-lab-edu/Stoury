package com.stoury.service;

import com.stoury.domain.ChatMessage;
import com.stoury.domain.ChatRoom;
import com.stoury.domain.Member;
import com.stoury.dto.chat.ChatMessageResponse;
import com.stoury.dto.chat.ChatRoomResponse;
import com.stoury.exception.ChatRoomSearchException;
import com.stoury.exception.authentication.NotAuthorizedException;
import com.stoury.exception.member.MemberSearchException;
import com.stoury.repository.ChatMessageRepository;
import com.stoury.repository.ChatRoomRepository;
import com.stoury.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatRoomResponse createChatRoom(Long senderId, Long receiverId) {
        Member sender = memberRepository.findById(senderId).orElseThrow(MemberSearchException::new);
        Member receiver = memberRepository.findById(receiverId).orElseThrow(MemberSearchException::new);

        ChatRoom chatRoom = new ChatRoom(List.of(sender, receiver));
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        return ChatRoomResponse.from(savedChatRoom);
    }

    @Transactional
    public ChatMessageResponse createChatMessage(Long senderId, Long chatRoomId, String textContent) {
        Member sender = memberRepository.findById(senderId).orElseThrow(MemberSearchException::new);
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(ChatRoomSearchException::new);
        if (!chatRoom.getMembers().contains(sender)) {
            throw new NotAuthorizedException("You're not a member of the chat room.");
        }
        if (!StringUtils.hasText(textContent)) {
            throw new IllegalArgumentException("Message content cannot be empty.");
        }

        ChatMessage chatMessage = new ChatMessage(sender, chatRoom, textContent);
        ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);

        return ChatMessageResponse.from(savedChatMessage);
    }
}
