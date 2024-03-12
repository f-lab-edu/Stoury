package com.stoury.dto.comment;

import com.stoury.domain.Comment;
import com.stoury.dto.SimpleMemberResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

public record CommentResponse(Long id, SimpleMemberResponse writer, Long feedId,
                              boolean hasNestedComments, String textContent, LocalDateTime createdAt) {
    public static CommentResponse from(Comment comment, Predicate<Comment> hasNestedComments) {
        return new CommentResponse(
                comment.getId(),
                SimpleMemberResponse.from(comment.getMember()),
                comment.getFeed().getId(),
                hasNestedComments.test(comment),
                comment.isDeleted() ? Comment.DELETED_CONTENT_TEXT : comment.getTextContent(),
                comment.getCreatedAt()
        );
    }

    public static List<CommentResponse> from(List<Comment> comments, Predicate<Comment> hasNestedComments) {
        return comments.stream()
                .map(comment -> from(comment, hasNestedComments))
                .toList();
    }
}
