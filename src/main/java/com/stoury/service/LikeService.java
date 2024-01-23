package com.stoury.service;

import com.stoury.domain.Feed;
import com.stoury.domain.Like;
import com.stoury.domain.Member;
import com.stoury.exception.AlreadyLikedFeedException;
import com.stoury.exception.FeedSearchException;
import com.stoury.exception.MemberCrudExceptions;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.LikeRepository;
import com.stoury.repository.MemberRepository;
import com.stoury.utils.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final FeedRepository feedRepository;
    private final MemberRepository memberRepository;
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
        Validator.of(liker)
                .willCheck(m -> memberRepository.existsById(m.getId()))
                .ifFailThrowsWithMessage(MemberCrudExceptions.MemberSearchException.class, "Member not found")
                .validate();
        Validator.of(feed)
                .willCheck(f -> feedRepository.existsById(f.getId()))
                .ifFailThrowsWithMessage(FeedSearchException.class, "Feed not found")
                .validate();
    }
}
