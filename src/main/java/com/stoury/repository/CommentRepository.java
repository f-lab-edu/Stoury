package com.stoury.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stoury.domain.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.stoury.domain.QComment.comment;
import static com.stoury.domain.QMember.member;

@Transactional
@Repository
@RequiredArgsConstructor
public class CommentRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    public Comment save(Comment saveComment) {
        entityManager.persist(saveComment);
        return saveComment;
    }

    @Transactional(readOnly = true)
    public List<Comment> findAllByFeedAndIdLessThanAndParentCommentIsNull(Feed feed, Long offsetId, Pageable pageable) {
        return jpaQueryFactory
                .selectFrom(comment)
                .leftJoin(comment.member, member).fetchJoin()
                .leftJoin(comment.feed, QFeed.feed).fetchJoin()
                .where(comment.id.lt(offsetId)
                        .and(QFeed.feed.eq(feed)))
                .orderBy(comment.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<Comment> findAllByParentCommentAndIdLessThan(Comment parentComment, Long offsetId, Pageable pageable) {
        return jpaQueryFactory
                .select(comment)
                .from(comment)
                .leftJoin(comment.member, member).fetchJoin()
                .leftJoin(comment.feed, QFeed.feed).fetchJoin()
                .leftJoin(comment.parentComment)
                .where(comment.id.lt(offsetId)
                        .and(comment.parentComment.eq(parentComment)))
                .orderBy(comment.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Transactional(readOnly = true)
    public Optional<Comment> findById(Long id) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(comment)
                .leftJoin(comment.member, member).fetchJoin()
                .leftJoin(comment.feed, QFeed.feed).fetchJoin()
                .leftJoin(comment.parentComment).fetchJoin()
                .where(comment.id.eq(id))
                .fetchFirst());
    }

    public void deleteAll() {
        jpaQueryFactory.selectFrom(comment).fetch().forEach(entityManager::remove);
    }
}
