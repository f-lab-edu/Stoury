package com.stoury.dto;

import com.stoury.domain.Comment;

import java.time.LocalDateTime;

public record NestedCommentResponse(Long id, WriterResponse writerResponse, Long parentCommentId,
                                    String textContent, LocalDateTime createdAt) {
    public static NestedCommentResponse from(Comment savedComment) {
        return new NestedCommentResponse(
                savedComment.getId(),
                WriterResponse.from(savedComment.getMember()),
                savedComment.getParentComment().getId(),
                savedComment.getTextContent(),
                savedComment.getCreatedAt()
        );
    }
}
