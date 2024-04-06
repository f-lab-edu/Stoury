package com.stoury.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stoury.domain.ChatMessage;
import com.stoury.domain.ChatRoom;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.stoury.domain.QChatMessage.*;

@Repository
@RequiredArgsConstructor
public class ChatMessageRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    @Transactional
    public ChatMessage save(ChatMessage saveChatMessage) {
        entityManager.persist(saveChatMessage);
        entityManager.refresh(saveChatMessage);
        return saveChatMessage;
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> findAllByChatRoomAndIdLessThan(ChatRoom chatRoom, Long offsetId, Pageable pageable){
        return jpaQueryFactory
                .selectFrom(chatMessage)
                .where(chatMessage.chatRoom.eq(chatRoom)
                        .and(chatMessage.id.lt(offsetId)))
                .orderBy(chatMessage.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Transactional
    public List<ChatMessage> saveAll(List<ChatMessage> saveChatMessages) {
        saveChatMessages.forEach(entityManager::persist);
        return saveChatMessages;
    }

    @Transactional
    public void deleteAll() {
        jpaQueryFactory.selectFrom(chatMessage).fetch().forEach(entityManager::remove);
    }
}
