package com.stoury.repository;

import com.stoury.domain.Comment;
import com.stoury.domain.Feed;
import com.stoury.dto.comment.CommentResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("""
            SELECT new com.stoury.dto.comment.CommentResponse(c,
            CASE WHEN (SELECT COUNT(nc) FROM Comment nc WHERE nc.parentComment = c) > 0 THEN true ELSE false END )
            FROM Comment c
            LEFT JOIN c.feed f
            WHERE c.createdAt < :orderThan
            AND f = :feed
            """)
    List<CommentResponse> findAllByFeedAndCreatedAtBeforeAndParentCommentIsNull(Feed feed, LocalDateTime orderThan, Pageable pageable);

    List<Comment> findAllByParentCommentAndCreatedAtBefore(Comment parentComment, LocalDateTime orderThan, Pageable pageable);

    boolean existsByParentComment(Comment parentComment);
}
