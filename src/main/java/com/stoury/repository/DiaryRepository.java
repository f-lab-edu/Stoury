package com.stoury.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stoury.domain.Diary;
import com.stoury.domain.Member;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.stoury.domain.QDiary.*;
import static com.stoury.domain.QGraphicContent.*;

@Repository
@RequiredArgsConstructor
public class DiaryRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    public Diary save(Diary saveDiary) {
        entityManager.persist(saveDiary);
        return saveDiary;
    }

    public List<Diary> findByMemberAndIdLessThan(Member member, Long offsetId, Pageable page) {
        return jpaQueryFactory
                .selectFrom(diary).leftJoin(diary.thumbnail, graphicContent)
                .where(diary.member.eq(member)
                        .and(diary.id.lt(offsetId)))
                .offset(page.getOffset())
                .limit(page.getPageSize())
                .fetch();
    }

    public Optional<Diary> findById(Long id) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(diary)
                .where(diary.id.eq(id))
                .fetchFirst());
    }

    public void delete(Diary deleteDiary) {
        jpaQueryFactory.delete(diary)
                .where(diary.eq(deleteDiary));
    }
}
