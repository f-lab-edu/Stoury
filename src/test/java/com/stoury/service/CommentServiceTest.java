package com.stoury.service;

import com.stoury.domain.Comment;
import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import com.stoury.repository.CommentRepository;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
    @Mock
    ValidateService validateService;
    @Mock
    CommentRepository commentRepository;

    @InjectMocks
    CommentService commentService;

    @Test
    @DisplayName("댓글 생성 성공")
    void createCommentSuccess() {
        Member dummyWriter = Member.builder().build();
        Feed dummyFeed = Feed.builder().build();
        String commentText = "dummy Comment!";
        when(commentRepository.save(any(Comment.class))).thenReturn(new Comment(dummyWriter, dummyFeed, commentText));

        commentService.createComment(dummyWriter, dummyFeed, commentText);

        verify(commentRepository, times(1)).save(any(Comment.class));
    }
}
