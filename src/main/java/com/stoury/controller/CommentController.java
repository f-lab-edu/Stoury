package com.stoury.controller;

import com.stoury.dto.comment.ChildCommentResponse;
import com.stoury.dto.comment.CommentResponse;
import com.stoury.dto.member.AuthenticatedMember;
import com.stoury.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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

    @GetMapping("/comments/feed/{feedId}")
    public List<CommentResponse> getComments(@PathVariable Long feedId,
                                             @RequestParam(required = false, defaultValue = "2100-12-31T00:00:00")
                                             LocalDateTime orderThan) {
        return commentService.getCommentsOfFeed(feedId, orderThan);
    }

    @GetMapping("/comments/comment/{commentId}")
    public List<ChildCommentResponse> getChildComments(@PathVariable Long commentId,
                                                       @RequestParam(required = false, defaultValue = "2100-12-31T00:00:00")
                                                       LocalDateTime orderThan) {
        return commentService.getChildComments(commentId, orderThan);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Object> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }
}
