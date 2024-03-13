package com.stoury.dto.comment;

import com.stoury.domain.Comment;
import com.stoury.dto.SimpleMemberResponse;

import java.time.LocalDateTime;

public record CommentResponse(Long id, SimpleMemberResponse writer, Long feedId,
                              boolean hasNestedComments, String textContent, LocalDateTime createdAt) {

    public CommentResponse(Comment comment, boolean hasNestedComments) {
        this(comment.getId(),
                SimpleMemberResponse.from(comment.getMember()),
                comment.getFeed().getId(),
                hasNestedComments,
                comment.isDeleted() ? Comment.DELETED_CONTENT_TEXT : comment.getTextContent(),
                comment.getCreatedAt());
    }

    public static CommentResponse from(Comment comment, boolean hasNestedComments) {
        return new CommentResponse(comment, hasNestedComments);
    }
}
