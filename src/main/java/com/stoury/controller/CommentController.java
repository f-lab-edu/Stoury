package com.stoury.controller;

import com.stoury.dto.comment.ChildCommentResponse;
import com.stoury.dto.comment.CommentResponse;
import com.stoury.dto.member.AuthenticatedMember;
import com.stoury.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/comments/feed/{feedId}")
    public CommentResponse createComment(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                                         @PathVariable Long feedId,
                                         @RequestBody String textContent) {
        return commentService.createComment(authenticatedMember.getId(), feedId, textContent);
    }

    @PostMapping("/comments/comment/{commentId}")
    public ChildCommentResponse createChildComment(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                                                   @PathVariable Long commentId,
                                                   @RequestBody String textContent) {
        return commentService.createNestedComment(authenticatedMember.getId(), commentId, textContent);
    }
}
