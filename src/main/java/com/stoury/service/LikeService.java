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
    private final LikeRepository likeRepository;
    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;

    @Transactional
    public void like(Long likerId, Long feedId) {
        Member liker = memberRepository.findById(Objects.requireNonNull(likerId))
                .orElseThrow(MemberSearchException::new);
        Feed feed = feedRepository.findById(Objects.requireNonNull(feedId))
                .orElseThrow(FeedSearchException::new);

        if (likeRepository.existsByMemberAndFeed(liker, feed)) {
            throw new AlreadyLikedFeedException("You already liked the feed");
        }

        Like like = new Like(liker, feed);
        likeRepository.save(like);
    }

    @Transactional
    public void likeCancel(Long likerId, Long feedId) {
        likeRepository.deleteByMemberAndFeed(likerId.toString(), feedId.toString());
    }

    @Transactional
    public long getLikesOfFeed(Long feedId) {
        Feed feed = feedRepository.findById(Objects.requireNonNull(feedId))
                .orElseThrow(FeedSearchException::new);
        return likeRepository.getCountByFeed(feed);
    }
}
