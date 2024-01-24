package com.stoury.dto;

import com.stoury.domain.Comment;

import java.time.LocalDateTime;

public record CommentResponse(Long id, WriterResponse writerResponse, Long feedId,
                              String textContent, LocalDateTime createdAt) {
    public static CommentResponse from(Comment savedComment) {
        return new CommentResponse(
                savedComment.getId(),
                WriterResponse.from(savedComment.getMember()),
                savedComment.getFeed().getId(),
                savedComment.getTextContent(),
                savedComment.getCreatedAt()
        );
    }
}
