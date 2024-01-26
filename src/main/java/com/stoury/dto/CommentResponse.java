package com.stoury.dto;

import com.stoury.domain.Comment;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(Long id, WriterResponse writerResponse, Long feedId,
                              boolean hasNestedComments, String textContent, LocalDateTime createdAt) {
    public static CommentResponse from(Comment savedComment) {
        return new CommentResponse(
                savedComment.getId(),
                WriterResponse.from(savedComment.getMember()),
                savedComment.getFeed().getId(),
                savedComment.hasNestedComments(),
                savedComment.getTextContent(),
                savedComment.getCreatedAt()
        );
    }

    public static List<CommentResponse> from(List<Comment> comments) {
        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }
}
