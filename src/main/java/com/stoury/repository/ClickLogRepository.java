package com.stoury.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stoury.domain.ClickLog;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static com.stoury.domain.QClickLog.clickLog;

@Repository
@RequiredArgsConstructor
public class ClickLogRepository {
    private final EntityManager entityManager;
    private final JPAQueryFactory jpaQueryFactory;

    @Transactional
    public ClickLog save(ClickLog feedClick) {
        entityManager.persist(feedClick);
        return feedClick;
    }

    @Transactional
    public void deleteAll() {
        jpaQueryFactory.selectFrom(clickLog).fetch().forEach(entityManager::remove);
    }
}
