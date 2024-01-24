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
    private final FeedRepository feedRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CommentResponse createComment(Member member, Feed feed, String commentText) {
        validate(member, feed);

        Comment comment = new Comment(member, feed,
                Objects.requireNonNull(commentText, "Comment text can not be empty."));

        Comment savedComment = commentRepository.save(comment);

        return CommentResponse.from(savedComment);
    }

    private void validate(Member commentWriter, Feed feed) {
        Validator.of(commentWriter)
                .willCheck(m -> memberRepository.existsById(m.getId()))
                .ifFailThrowsWithMessage(MemberCrudExceptions.MemberSearchException.class, "Member not found")
                .validate();
        Validator.of(feed)
                .willCheck(f -> feedRepository.existsById(f.getId()))
                .ifFailThrowsWithMessage(FeedSearchException.class, "Feed not found")
                .validate();
    }
}
