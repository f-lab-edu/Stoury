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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
        Long senderIdNotNull = Objects.requireNonNull(senderId);
        Long chatRoomIdNotNull = Objects.requireNonNull(chatRoomId);
        if (!StringUtils.hasText(textContent)) {
            throw new IllegalArgumentException("Message content cannot be empty.");
        }

        Member sender = memberRepository.findById(senderIdNotNull).orElseThrow(MemberSearchException::new);
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomIdNotNull).orElseThrow(ChatRoomSearchException::new);
        if (chatRoom.hasMember(sender)) {
            ChatMessage chatMessage = new ChatMessage(sender, chatRoom, textContent);
            ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);

            return ChatMessageResponse.from(savedChatMessage);
        }
        throw new NotAuthorizedException("You're not a member of the chat room.");
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getPreviousChatMessages(Long memberId, Long chatRoomId, LocalDateTime orderThan) {
        Long senderIdNotNull = Objects.requireNonNull(memberId);
        Long chatRoomIdNotNull = Objects.requireNonNull(chatRoomId);
        if (orderThan == null) {
            orderThan = LocalDateTime.of(2100, 12, 31, 0, 0, 0);
        }

        Member member = memberRepository.findById(senderIdNotNull).orElseThrow(MemberSearchException::new);
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomIdNotNull).orElseThrow(ChatRoomSearchException::new);
        if (chatRoom.hasMember(member)) {
            Pageable pageable = PageRequest.of(0, 50, Sort.by("createdAt").descending());
            List<ChatMessage> chatMessages = chatMessageRepository.findAllByChatRoomAndCreatedAtBefore(chatRoom, orderThan, pageable);

            return chatMessages.stream().map(ChatMessageResponse::from).toList();
        }
        throw new NotAuthorizedException("You're not a member of the chat room.");
    }

    public void checkIfRoomMember(Long memberId, Long chatRoomId) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberSearchException::new);
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(ChatRoomSearchException::new);
        if (chatRoom.hasMember(member)) {
            return;
        }
        throw new NotAuthorizedException("You're not a member of the chat room.");
    }
}
