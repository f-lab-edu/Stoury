package com.stoury.service;

import com.stoury.domain.ChatRoom;
import com.stoury.domain.Member;
import com.stoury.dto.chat.ChatRoomResponse;
import com.stoury.exception.member.MemberCreateException;
import com.stoury.repository.ChatMessageRepository;
import com.stoury.repository.ChatRoomRepository;
import com.stoury.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatRoomResponse createChatRoom(Long senderId, Long receiverId) {
        Member sender = memberRepository.findById(senderId).orElseThrow(MemberCreateException::new);
        Member receiver = memberRepository.findById(receiverId).orElseThrow(MemberCreateException::new);

        ChatRoom chatRoom = new ChatRoom(List.of(sender, receiver));
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        return ChatRoomResponse.from(savedChatRoom);
    }
}
