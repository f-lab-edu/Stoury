package com.stoury.service;

import com.stoury.domain.Comment;
import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import com.stoury.dto.CommentResponse;
import com.stoury.dto.NestedCommentResponse;
import com.stoury.exception.CommentCreateException;
import com.stoury.exception.CommentSearchException;
import com.stoury.exception.feed.FeedSearchException;
import com.stoury.exception.member.MemberSearchException;
import com.stoury.repository.CommentRepository;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;

    @Transactional
    public CommentResponse createComment(Member member, Feed feed, String commentText) {
        validate(member);
        validate(feed);

        Comment comment = new Comment(member, feed,
                Objects.requireNonNull(commentText, "Comment text can not be empty."));

        Comment savedComment = commentRepository.save(comment);

        return CommentResponse.from(savedComment);
    }

    @Transactional
    public NestedCommentResponse createNestedComment(Member member, Comment parentComment, String commentText) {
        validate(member);
        validate(parentComment);

        Comment comment = new Comment(member, parentComment,
                Objects.requireNonNull(commentText, "Comment text can not be empty."));

        Comment savedComment = commentRepository.save(comment);

        return NestedCommentResponse.from(savedComment);
    }

    private void validate(Comment parentComment) {
        Long parentCommentId = Objects.requireNonNull(parentComment.getId(), "Parent Id cannot be null");
        if (!commentRepository.existsById(parentCommentId)) {
            throw new CommentSearchException("Comment not found");
        }
        if (parentComment.hasParent()) {
            throw new CommentCreateException("Nested comments are allowed in a level.");
        }
    }

    private void validate(Member commentWriter) {
        Long writerId = Objects.requireNonNull(commentWriter.getId(), "Liker Id cannot be null");
        if (!memberRepository.existsById(writerId)) {
            throw new MemberSearchException("Member not found");
        }
    }

    private void validate(Feed feed) {
        Long feedId = Objects.requireNonNull(feed.getId(), "Feed Id cannot be null");
        if (!feedRepository.existsById(feedId)) {
            throw new FeedSearchException("Feed not found");
        }
    }
}
