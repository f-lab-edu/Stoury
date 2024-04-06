package com.stoury.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stoury.domain.ChildComment;
import com.stoury.domain.Comment;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.stoury.domain.QChildComment.childComment;
import static com.stoury.domain.QMember.member;

@Transactional
@Repository
@RequiredArgsConstructor
public class ChildCommentRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;


    public ChildComment save(ChildComment saveComment) {
        entityManager.persist(saveComment);
        entityManager.refresh(saveComment);
        return saveComment;
    }


    @Transactional(readOnly = true)
    public List<ChildComment> findAllByParentComment(Comment parentComment, Long offsetId, Pageable pageable) {
        return jpaQueryFactory
                .selectFrom(childComment)
                .innerJoin(childComment.member, member).fetchJoin()
                .where(childComment.id.lt(offsetId)
                        .and(childComment.parentComment.eq(parentComment)))
                .orderBy(childComment.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    public void deleteAll() {
        jpaQueryFactory.selectFrom(childComment).fetch().forEach(entityManager::remove);
    }

    public Optional<ChildComment> findById(Long id) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(childComment)
                .where(childComment.id.eq(id))
                .fetchFirst());
    }
}
