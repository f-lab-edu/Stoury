package com.stoury.validator;

import com.stoury.domain.Comment;
import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import com.stoury.exception.CommentSearchException;
import com.stoury.exception.feed.FeedSearchException;
import com.stoury.exception.member.MemberSearchException;
import com.stoury.repository.CommentRepository;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class Validator {
    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;
    private final CommentRepository commentRepository;

    public void isMemberExists(Member member) {
        Long memberId = Objects.requireNonNull(member.getId(), "Liker Id cannot be null");
        if (!memberRepository.existsById(memberId)) {
            throw new MemberSearchException("Member not found");
        }
    }


    public void isFeedExists(Feed feed) {
        Long feedId = Objects.requireNonNull(feed.getId(), "Feed Id cannot be null");
        if (!feedRepository.existsById(feedId)) {
            throw new FeedSearchException("Feed not found");
        }
    }


    public void isCommentExists(Comment comment) {
        Long parentCommentId = Objects.requireNonNull(comment.getId(), "Comment Id cannot be null");
        if (!commentRepository.existsById(parentCommentId)) {
            throw new CommentSearchException("Comment not found");
        }
    }
}
