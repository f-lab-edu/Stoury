package com.stoury.service;

import com.stoury.domain.Comment;
import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import com.stoury.dto.CommentResponse;
import com.stoury.dto.ChildCommentResponse;
import com.stoury.exception.CommentCreateException;
import com.stoury.exception.CommentSearchException;
import com.stoury.repository.CommentRepository;
import com.stoury.validator.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentService {
    public final int PAGE_SIZE = 20;
    private final CommentRepository commentRepository;
    private final Validator validator;

    @Transactional
    public CommentResponse createComment(Member member, Feed feed, String commentText) {
        validator.isMemberExists(member);
        validator.isFeedExists(feed);

        Comment comment = new Comment(member, feed,
                Objects.requireNonNull(commentText, "Comment text can not be empty."));

        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional
    public ChildCommentResponse createNestedComment(Member member, Comment parentComment, String commentText) {
        validator.isMemberExists(member);
        if (parentComment.hasParent()) {
            throw new CommentCreateException("Nested comments are allowed in a level.");
        }
        validator.isCommentExists(parentComment);

        Comment comment = new Comment(member, parentComment,
                Objects.requireNonNull(commentText, "Comment text can not be empty."));

        return ChildCommentResponse.from(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsOfFeed(Feed feed, LocalDateTime orderThan) {
        validator.isFeedExists(feed);

        Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by("createdAt").descending());

        return CommentResponse.from(commentRepository.findAllByFeedAndCreatedAtBefore(feed, orderThan, pageable));
    }

    @Transactional(readOnly = true)
    public List<ChildCommentResponse> getNestedComments(Comment parentComment, LocalDateTime orderThan) {
        if (parentComment.hasParent()) {
            throw new CommentSearchException("Nested comments are allowed in a level.");
        }
        validator.isCommentExists(parentComment);

        Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by("createdAt").descending());

        return ChildCommentResponse.from(commentRepository.findAllByParentCommentAndCreatedAtBefore(parentComment,
                orderThan, pageable));
    }

    @Transactional
    public void deleteComment(Comment comment) {
        validator.isCommentExists(comment);

        comment.delete();
    }
}
