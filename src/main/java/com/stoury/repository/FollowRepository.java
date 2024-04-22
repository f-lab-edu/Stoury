package com.stoury.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stoury.domain.Follow;
import com.stoury.domain.QFollow;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static com.stoury.domain.QFollow.*;

@Repository
@RequiredArgsConstructor
public class FollowRepository {
    private final EntityManager entityManager;
    private final JPAQueryFactory jpaQueryFactory;

    @Transactional
    public Follow save(Follow follow) {
        entityManager.persist(follow);
        return follow;
    }

    @Transactional
    public void deleteAll() {
        jpaQueryFactory.selectFrom(follow).fetch().forEach(entityManager::remove);
    }
}
