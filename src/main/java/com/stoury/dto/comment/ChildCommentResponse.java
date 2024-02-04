package com.stoury.dto.comment;

import com.stoury.domain.Comment;
import com.stoury.dto.WriterResponse;

import java.time.LocalDateTime;
import java.util.List;

public record ChildCommentResponse(Long id, WriterResponse writerResponse, Long parentCommentId,
                                   String textContent, LocalDateTime createdAt) {
    public static ChildCommentResponse from(Comment comment) {
        return new ChildCommentResponse(
                comment.getId(),
                WriterResponse.from(comment.getMember()),
                comment.getParentComment().getId(),
                comment.isDeleted() ? Comment.DELETED_CONTENT_TEXT : comment.getTextContent(),
                comment.getCreatedAt()
        );
    }

    public static List<ChildCommentResponse> from(List<Comment> nestedComments) {
        return nestedComments.stream()
                .map(ChildCommentResponse::from)
                .toList();
    }
}
