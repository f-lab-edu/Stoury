package com.stoury.dto.comment;

import com.stoury.domain.ChildComment;
import com.stoury.domain.Comment;
import com.stoury.dto.SimpleMemberResponse;

import java.time.LocalDateTime;
import java.util.List;

public record ChildCommentResponse(Long id,
                                   SimpleMemberResponse writerResponse,
                                   Long parentCommentId,
                                   String textContent,
                                   LocalDateTime createdAt) {
    public static ChildCommentResponse from(ChildComment childComment) {
        return new ChildCommentResponse(
                childComment.getId(),
                SimpleMemberResponse.from(childComment.getMember()),
                childComment.getParentComment().getId(),
                childComment.isDeleted() ? Comment.DELETED_CONTENT_TEXT : childComment.getTextContent(),
                childComment.getCreatedAt()
        );
    }

    public static List<ChildCommentResponse> from(List<ChildComment> childComments) {
        return childComments.stream()
                .map(ChildCommentResponse::from)
                .toList();
    }
}
