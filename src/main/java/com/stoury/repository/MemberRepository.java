package com.stoury.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stoury.domain.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.stoury.domain.QMember.member;

@Repository
@RequiredArgsConstructor
@Transactional
public class MemberRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    public Member save(Member saveMember) {
        entityManager.persist(saveMember);
        return saveMember;
    }

    @Transactional(readOnly = true)
    public Optional<Member> findByEmail(String email) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(member)
                .where(member.email.eq(email))
                .fetchFirst());
    }

    @Transactional(readOnly = true)
    public Slice<Member> findMembersByUsernameMatches(String username, Pageable page){
        int pageSize = page.getPageSize();
        Query nativeQuery = entityManager.createNativeQuery("""
                SELECT *
                FROM MEMBER
                WHERE MATCH(USERNAME) AGAINST (:username'*' IN BOOLEAN MODE)
                LIMIT :start, :end
                """, Member.class);
        nativeQuery.setParameter("username", username);
        nativeQuery.setParameter("start", page.getOffset());
        nativeQuery.setParameter("end", page.getOffset() + pageSize + 1); // 페이지 크기보다 하나 더 가져와서 next가 존재하는지 확인

        List<Member> members = nativeQuery.getResultList();
        return getMemberSlice(members, page);
    }

    private Slice<Member> getMemberSlice(List<Member> foundMembers, Pageable page) {
        int pageSize = page.getPageSize();
        if (foundMembers.size() == pageSize + 1) {
            List<Member> content = foundMembers.subList(0, pageSize);
            return new SliceImpl<>(content, page, true);
        }
        return new SliceImpl<>(foundMembers, page, false);
    }

    @Transactional(readOnly = true)
    public List<Member> findAllByDeletedIsTrue(){
        return jpaQueryFactory
                .selectFrom(member)
                .where(member.deleted.isTrue())
                .fetch();
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email){
        return jpaQueryFactory
                .select(member.count())
                .from(member)
                .where(member.email.eq(email))
                .fetchFirst() > 0;
    }

    @Transactional(readOnly = true)
    public Optional<Member> findById(Long memberId) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(member)
                .where(member.id.eq(memberId))
                .fetchFirst());
    }

    @Transactional(readOnly = true)
    public Collection<Member> findAllById(Collection<Long> memberIds) {
        return jpaQueryFactory
                .selectFrom(member)
                .where(member.id.in(memberIds))
                .fetch();
    }

    public void deleteAll() {
        jpaQueryFactory.selectFrom(member).fetch().forEach(entityManager::remove);
    }

    public List<Member> saveAll(Collection<Member> members) {
        members.forEach(entityManager::persist);
        return members.stream().toList();
    }

    public List<Member> saveAllAndFlush(Collection<Member> members) {
        List<Member> savedMembers = saveAll(members);
        entityManager.flush();
        return savedMembers;
    }
}
