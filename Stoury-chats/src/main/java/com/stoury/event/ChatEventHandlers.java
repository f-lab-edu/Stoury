package com.stoury.event;

import com.stoury.domain.ChatMessage;
import com.stoury.domain.ChatRoom;
import com.stoury.domain.Member;
import com.stoury.exception.chat.ChatRoomSearchException;
import com.stoury.exception.member.MemberSearchException;
import com.stoury.repository.ChatMessageRepository;
import com.stoury.repository.ChatRoomRepository;
import com.stoury.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ChatEventHandlers {
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onChatMessageSaveEventHandler(ChatMessageSaveEvent chatMessageSaveEvent) {
        Long memberId = Objects.requireNonNull(chatMessageSaveEvent.getMemberId());
        Long chatRoomId = Objects.requireNonNull(chatMessageSaveEvent.getChatRoomId());
        String textContent = stringNonEmpty(chatMessageSaveEvent.getTextContent());

        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(MemberSearchException::new);
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomSearchException::new);

        ChatMessage chatMessage = new ChatMessage(member, chatRoom, textContent);
        chatMessageRepository.save(chatMessage);
    }

    private String stringNonEmpty(String textContent) {
        if (!StringUtils.hasText(textContent)) {
            throw new IllegalArgumentException();
        }
        return textContent;
    }
}
