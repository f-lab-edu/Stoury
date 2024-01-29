package com.stoury.repository;

import com.stoury.domain.Comment;
import com.stoury.domain.Feed;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByFeedAndCreatedAtBefore(Feed feed, LocalDateTime orderThan, Pageable pageable);

    List<Comment> findAllByParentCommentAndCreatedAtBefore(Comment parentComment, LocalDateTime orderThan, Pageable pageable);
}
