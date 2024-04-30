package com.stoury.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stoury.domain.Diary;
import com.stoury.domain.Member;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.stoury.domain.QDiary.diary;
import static com.stoury.domain.QGraphicContent.graphicContent;

@Repository
@RequiredArgsConstructor
public class DiaryRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    @Transactional
    public Diary save(Diary saveDiary) {
        entityManager.persist(saveDiary);
        entityManager.refresh(saveDiary);
        return saveDiary;
    }

    @Transactional(readOnly = true)
    public List<Diary> findByMemberAndIdLessThan(Member member, Long offsetId, Pageable page) {
        return jpaQueryFactory
                .selectFrom(diary)
                .leftJoin(diary.thumbnail, graphicContent)
                .where(diary.member.eq(member)
                        .and(diary.id.lt(offsetId)))
                .orderBy(diary.id.desc())
                .offset(page.getOffset())
                .limit(page.getPageSize())
                .fetch();
    }

    @Transactional(readOnly = true)
    public Optional<Diary> findById(Long id) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(diary)
                .where(diary.id.eq(id))
                .fetchFirst());
    }

    @Transactional
    public void delete(Diary deleteDiary) {
        jpaQueryFactory.delete(diary)
                .where(diary.eq(deleteDiary));
    }

    @Transactional
    public void deleteAll() {
        jpaQueryFactory.delete(diary).execute();
    }

    @Transactional
    public Diary saveAndFlush(Diary diary) {
        Diary savedDiary = save(diary);
        entityManager.flush();
        return savedDiary;
    }
}
