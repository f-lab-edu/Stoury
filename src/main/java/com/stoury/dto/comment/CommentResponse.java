package com.stoury.dto.comment;

import com.stoury.domain.Comment;
import com.stoury.dto.WriterResponse;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(Long id, WriterResponse writerResponse, Long feedId,
                              boolean hasNestedComments, String textContent, LocalDateTime createdAt) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                WriterResponse.from(comment.getMember()),
                comment.getFeed().getId(),
                comment.hasNestedComments(),
                comment.isDeleted() ? Comment.DELETED_CONTENT_TEXT : comment.getTextContent(),
                comment.getCreatedAt()
        );
    }

    public static List<CommentResponse> from(List<Comment> comments) {
        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }
}