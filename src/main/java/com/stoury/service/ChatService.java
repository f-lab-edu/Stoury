package com.stoury.service;

import com.stoury.config.sse.SseEmitters;
import com.stoury.domain.ChatMessage;
import com.stoury.domain.ChatRoom;
import com.stoury.domain.Member;
import com.stoury.dto.chat.ChatMessageResponse;
import com.stoury.dto.chat.ChatRoomResponse;
import com.stoury.event.ChatMessageSaveEvent;
import com.stoury.exception.chat.ChatRoomSearchException;
import com.stoury.exception.authentication.NotAuthorizedException;
import com.stoury.exception.member.MemberSearchException;
import com.stoury.repository.ChatMessageRepository;
import com.stoury.repository.ChatRoomRepository;
import com.stoury.repository.MemberRepository;
import com.stoury.service.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SseEmitters sseEmitters;
    private final ApplicationEventPublisher eventPublisher;
    private final KafkaProducer kafkaProducer;

    @Transactional
    public ChatRoomResponse createChatRoom(Long senderId, Long receiverId) {
        List<Member> members = memberRepository.findAllById(List.of(senderId, receiverId));

        ChatRoom chatRoom = new ChatRoom(members);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        return ChatRoomResponse.from(savedChatRoom);
    }

    protected ChatMessageResponse publishChatMessageSaveEvent(Long senderId, Long chatRoomId, String textContent) {
        Member sender = memberRepository.findById(senderId).orElseThrow(MemberSearchException::new);

        LocalDateTime createdAt = LocalDateTime.now();
        ChatMessageSaveEvent chatMessageSaveEvent = ChatMessageSaveEvent.builder()
                .source(this)
                .memberId(senderId)
                .chatRoomId(chatRoomId)
                .textContent(textContent)
                .createdAt(createdAt)
                .build();
        eventPublisher.publishEvent(chatMessageSaveEvent);

        return ChatMessageResponse.from(chatMessageSaveEvent, sender);
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

    @Transactional
    public ChatMessageResponse sendChatMessage(Long memberId, Long chatRoomId, String textContent) {
        checkIfRoomMember(memberId, chatRoomId);
        if (!StringUtils.hasText(textContent)) {
            throw new IllegalArgumentException("Message content cannot be empty.");
        }

        ChatMessageResponse chatMessage = publishChatMessageSaveEvent(memberId, chatRoomId, textContent);

        kafkaProducer.produce(chatMessage);
        return chatMessage;
    }

    protected void checkIfRoomMember(Long memberId, Long chatRoomId) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberSearchException::new);
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(ChatRoomSearchException::new);
        if (chatRoom.hasMember(member)) {
            return;
        }
        throw new NotAuthorizedException("You're not a member of the chat room.");
    }

    @Transactional(readOnly = true)
    public SseEmitter enterChatRoom(Long memberId, Long chatRoomId) {
        checkIfRoomMember(memberId, chatRoomId);
        return sseEmitters.get(chatRoomId);
    }
}
