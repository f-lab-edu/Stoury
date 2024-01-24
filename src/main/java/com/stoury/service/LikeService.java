package com.stoury.service;

import com.stoury.domain.Feed;
import com.stoury.domain.Like;
import com.stoury.domain.Member;
import com.stoury.exception.AlreadyLikedFeedException;
import com.stoury.exception.feed.FeedSearchException;
import com.stoury.exception.member.MemberSearchException;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.LikeRepository;
import com.stoury.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;
    private final LikeRepository likeRepository;

    @Transactional
    public void like(Member liker, Feed feed) {
        validate(liker, feed);

        if (likeRepository.existsByMemberAndFeed(liker, feed)) {
            throw new AlreadyLikedFeedException("You already liked the feed");
        }

        Like like = new Like(liker, feed);
        likeRepository.save(like);
    }

    @Transactional
    public void likeCancel(Member canceler, Feed feed) {
        validate(canceler, feed);

        likeRepository.deleteByMemberAndFeed(canceler, feed);
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
