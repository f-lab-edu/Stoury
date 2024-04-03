package com.stoury.repository;

import com.stoury.domain.Comment;
import com.stoury.domain.Feed;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("""
    SELECT c
    FROM Comment c
    JOIN FETCH c.member
    LEFT JOIN c.feed f
    WHERE c.id < :offsetId
    AND f = :feed
    """)
    List<Comment> findAllByFeedAndIdLessThanAndParentCommentIsNull(Feed feed, Long offsetId, Pageable pageable);

    @Query("""
    SELECT c
    FROM Comment c
    JOIN FETCH c.member
    LEFT JOIN c.parentComment c2
    WHERE c.id < :offsetId
    AND c2 = :parentComment
    """)
    List<Comment> findAllByParentCommentAndIdLessThan(Comment parentComment, Long offsetId, Pageable pageable);
}
