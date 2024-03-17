package com.stoury.repository;

import com.stoury.domain.Comment;
import com.stoury.domain.Feed;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("""
    SELECT c
    FROM Comment c
    JOIN FETCH c.member
    LEFT JOIN c.feed f
    WHERE c.createdAt < :orderThan
    AND f = :feed
    """)
    List<Comment> findAllByFeedAndCreatedAtBeforeAndParentCommentIsNull(Feed feed, LocalDateTime orderThan, Pageable pageable);

    @Query("""
    SELECT c
    FROM Comment c
    JOIN FETCH c.member
    LEFT JOIN c.parentComment c2
    WHERE c.createdAt < :orderThan
    AND c2 = :parentComment
    """)
    List<Comment> findAllByParentCommentAndCreatedAtBefore(Comment parentComment, LocalDateTime orderThan, Pageable pageable);
}
