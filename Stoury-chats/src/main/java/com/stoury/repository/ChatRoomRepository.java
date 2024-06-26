package com.stoury.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stoury.domain.ChatRoom;
import com.stoury.domain.Member;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static com.stoury.domain.QChatRoom.chatRoom;
import static com.stoury.domain.QMember.member;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    @Transactional
    public ChatRoom save(ChatRoom saveChatRoom) {
        entityManager.persist(saveChatRoom);
        return saveChatRoom;
    }

    @Transactional(readOnly = true)
    public boolean existsBy(Set<Member> members) {
        return jpaQueryFactory
                .selectFrom(chatRoom).leftJoin(chatRoom.members, member)
                .where(member.in(members))
                .groupBy(chatRoom.id)
                .having(chatRoom.count().eq((long) members.size()))
                .fetchFirst() != null;
    }

    @Transactional(readOnly = true)
    public Optional<ChatRoom> findById(Long id) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(chatRoom)
                .where(chatRoom.id.eq(id))
                .fetchFirst());
    }

    @Transactional
    public void deleteAll() {
        jpaQueryFactory.delete(chatRoom).execute();
    }
}
