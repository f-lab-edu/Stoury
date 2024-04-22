package com.stoury.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stoury.domain.Follow;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FollowRepository {
    private final EntityManager entityManager;
    private final JPAQueryFactory jpaQueryFactory;

    public Follow save(Follow follow) {
        entityManager.persist(follow);
        return follow;
    }
}
