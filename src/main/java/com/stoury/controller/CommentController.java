package com.stoury.controller;

import com.stoury.dto.comment.ChildCommentResponse;
import com.stoury.dto.comment.CommentResponse;
import com.stoury.dto.member.AuthenticatedMember;
import com.stoury.service.CommentService;
import com.stoury.utils.Values;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        return commentService.createChildComment(authenticatedMember.getId(), commentId, textContent);
    }

    @GetMapping("/comments/feed/{feedId}")
    public List<CommentResponse> getComments(@PathVariable Long feedId,
                                             @RequestParam(required = false, defaultValue = Values.MAX_LONG) Long cursorId) {
        return commentService.getCommentsOfFeed(feedId, cursorId);
    }

    @GetMapping("/comments/comment/{commentId}")
    public List<ChildCommentResponse> getChildComments(@PathVariable Long commentId,
                                                       @RequestParam(required = false, defaultValue = Values.MAX_LONG) Long cursorId) {
        return commentService.getChildComments(commentId, cursorId);
    }

    @DeleteMapping("/comments/{commentId}")
    public void deleteComment(@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
                              @PathVariable Long commentId) {
        commentService.deleteCommentIfOwner(commentId, authenticatedMember.getId());
    }
}
