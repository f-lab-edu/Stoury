package com.stoury.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stoury.domain.Follow;
import com.stoury.domain.Member;
import com.stoury.domain.QFollow;
import com.stoury.domain.QMember;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.stoury.domain.QFollow.*;
import static com.stoury.domain.QMember.*;

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

    @Transactional(readOnly = true)
    public long countFollowingNumbersOf(Member follower) {
        return jpaQueryFactory.select(follow.count())
                .from(follow).join(follow.follower, member)
                .where(member.eq(follower))
                .fetchFirst();
    }

    @Transactional(readOnly = true)
    public List<Member> findFollowerId(Long followeeId) {
        return jpaQueryFactory
                .select(follow.follower)
                .from(follow)
                .where(follow.followee.id.eq(followeeId))
                .fetch();
    }
}
