package com.stoury.service;

import com.stoury.domain.Comment;
import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import com.stoury.dto.CommentResponse;
import com.stoury.exception.FeedSearchException;
import com.stoury.exception.MemberCrudExceptions;
import com.stoury.repository.CommentRepository;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.MemberRepository;
import com.stoury.utils.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ValidateService validateService;

    @Transactional
    public CommentResponse createComment(Member member, Feed feed, String commentText) {
        validateService.validate(member);
        validateService.validate(feed);
        
        Comment comment = new Comment(member, feed,
                Objects.requireNonNull(commentText, "Comment text can not be empty."));

        Comment savedComment = commentRepository.save(comment);

        return CommentResponse.from(savedComment);
    }
}
