package com.stoury.event;

import com.stoury.domain.ChatMessage;
import com.stoury.domain.ChatRoom;
import com.stoury.domain.Member;
import com.stoury.exception.chat.ChatRoomSearchException;
import com.stoury.exception.member.MemberSearchException;
import com.stoury.repository.ChatMessageRepository;
import com.stoury.repository.ChatRoomRepository;
import com.stoury.repository.MemberRepository;
import com.stoury.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class EventHandlers {
    private final StorageService storageService;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFileSaveEventHandler(GraphicSaveEvent graphicSaveEvent) {
        MultipartFile fileToSave = graphicSaveEvent.getFileToSave();
        String path = graphicSaveEvent.getPath();
        storageService.saveFileAtPath(fileToSave, Paths.get(path));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFileDeleteEventHandler(GraphicDeleteEvent graphicDeleteEvent) {
        String path = graphicDeleteEvent.getPath();
        storageService.deleteFileAtPath(Paths.get(path));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onChatMessageSaveEventHandler(ChatMessageSaveEvent chatMessageSaveEvent) {
        Long memberId = Objects.requireNonNull(chatMessageSaveEvent.getMemberId());
        Long chatRoomId = Objects.requireNonNull(chatMessageSaveEvent.getChatRoomId());
        String textContent = stringNonEmpty(chatMessageSaveEvent.getTextContent());
        LocalDateTime createdAt = Objects.requireNonNull(chatMessageSaveEvent.getCreatedAt());

        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(MemberSearchException::new);
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomSearchException::new);

        ChatMessage chatMessage = new ChatMessage(member, chatRoom, textContent, createdAt);
        chatMessageRepository.save(chatMessage);
    }

    private String stringNonEmpty(String textContent) {
        if (!StringUtils.hasText(textContent)) {
            throw new IllegalArgumentException();
        }
        return textContent;
    }
}
