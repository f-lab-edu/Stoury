package com.stoury.service;

import com.stoury.domain.Comment;
import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import com.stoury.dto.CommentResponse;
import com.stoury.exception.feed.FeedSearchException;
import com.stoury.exception.member.MemberSearchException;
import com.stoury.repository.CommentRepository;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;

    @Transactional
    public CommentResponse createComment(Member member, Feed feed, String commentText) {
        validate(member, feed);

        Comment comment = new Comment(member, feed,
                Objects.requireNonNull(commentText, "Comment text can not be empty."));

        Comment savedComment = commentRepository.save(comment);

        return CommentResponse.from(savedComment);
    }

    private void validate(Member liker, Feed feed) {
        Long likerId = Objects.requireNonNull(liker.getId(), "Liker Id cannot be null");
        if (!memberRepository.existsById(likerId)) {
            throw new MemberSearchException("Member not found");
        }

        Long feedId = Objects.requireNonNull(feed.getId(), "Feed Id cannot be null");
        if (!feedRepository.existsById(feedId)) {
            throw new FeedSearchException("Feed not found");
        }
    }
}
