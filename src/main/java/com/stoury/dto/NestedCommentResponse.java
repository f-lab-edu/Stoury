package com.stoury.dto;

import com.stoury.domain.Comment;

import java.time.LocalDateTime;
import java.util.List;

public record NestedCommentResponse(Long id, WriterResponse writerResponse, Long parentCommentId,
                                    String textContent, LocalDateTime createdAt) {
    public static NestedCommentResponse from(Comment comment) {
        return new NestedCommentResponse(
                comment.getId(),
                WriterResponse.from(comment.getMember()),
                comment.getParentComment().getId(),
                comment.isDeleted() ? Comment.DELETED_CONTENT_TEXT : comment.getTextContent(),
                comment.getCreatedAt()
        );
    }

    public static List<NestedCommentResponse> from(List<Comment> nestedComments) {
        return nestedComments.stream()
                .map(NestedCommentResponse::from)
                .toList();
    }
}
