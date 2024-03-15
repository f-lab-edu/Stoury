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
    SELECT C 
    FROM Comment C JOIN FETCH C.member
    WHERE C.feed = :feed AND C.createdAt < :orderThan AND C.parentComment IS NULL
    """)
    List<Comment> findAllByFeedAndCreatedAtBeforeAndParentCommentIsNull(Feed feed, LocalDateTime orderThan, Pageable pageable);

    @Query("""
    SELECT C 
    FROM Comment C JOIN FETCH C.member
    WHERE C.parentComment = :parentComment AND C.createdAt < :orderThan
    """)
    List<Comment> findAllByParentCommentAndCreatedAtBefore(Comment parentComment, LocalDateTime orderThan, Pageable pageable);
}
