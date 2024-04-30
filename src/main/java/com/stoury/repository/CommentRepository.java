package com.stoury.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stoury.domain.Comment;
import com.stoury.domain.Feed;
import com.stoury.domain.QFeed;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.stoury.domain.QComment.comment;
import static com.stoury.domain.QMember.member;

@Repository
@RequiredArgsConstructor
public class CommentRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    @Transactional
    public Comment save(Comment saveComment) {
        entityManager.persist(saveComment);
        entityManager.refresh(saveComment);
        return saveComment;
    }

    @Transactional(readOnly = true)
    public List<Comment> findAllByFeedAndIdLessThan(Feed feed, Long offsetId, Pageable pageable) {
        return jpaQueryFactory
                .selectFrom(comment)
                .innerJoin(comment.member, member).fetchJoin()
                .where(comment.id.lt(offsetId)
                        .and(comment.feed.eq(feed)))
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
                .where(comment.id.eq(id))
                .fetchFirst());
    }

    @Transactional
    public void deleteAll() {
        jpaQueryFactory.delete(comment).execute();
    }
}
