package com.stoury.service;

import com.stoury.domain.Feed;
import com.stoury.domain.Like;
import com.stoury.domain.Member;
import com.stoury.exception.AlreadyLikedFeedException;
import com.stoury.exception.feed.FeedSearchException;
import com.stoury.exception.member.MemberSearchException;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.LikeRedisRepository;
import com.stoury.repository.LikeRepository;
import com.stoury.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeJpaRepository;
    private final LikeRedisRepository likeRedisRepository;
    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;

    @Transactional
    public void like(Long likerId, Long feedId) {
        Member liker = memberRepository.findById(Objects.requireNonNull(likerId))
                .orElseThrow(MemberSearchException::new);
        Feed feed = feedRepository.findById(Objects.requireNonNull(feedId))
                .orElseThrow(FeedSearchException::new);

        if (likeRedisRepository.existsByMemberAndFeed(liker, feed) || likeJpaRepository.existsByMemberAndFeed(liker, feed)) {
            throw new AlreadyLikedFeedException("You already liked the feed");
        }

        Like like = new Like(liker, feed);
        likeRedisRepository.save(like);
    }

    @Transactional
    public void likeCancel(Long likerId, Long feedId) {
        Member liker = memberRepository.findById(Objects.requireNonNull(likerId))
                .orElseThrow(MemberSearchException::new);
        Feed feed = feedRepository.findById(Objects.requireNonNull(feedId))
                .orElseThrow(FeedSearchException::new);

        if (likeRedisRepository.deleteByMemberAndFeed(liker, feed)) {
            return;
        }
        likeJpaRepository.deleteByMemberAndFeed(liker, feed);
    }

    @Transactional
    public int getLikesOfFeed(Long feedId) {
        Feed feed = feedRepository.findById(Objects.requireNonNull(feedId))
                .orElseThrow(FeedSearchException::new);

        return likeRedisRepository.countByFeed(feed)
                + likeJpaRepository.countByFeed(feed);
    }
}
