package com.stoury.service;

import com.stoury.domain.Feed;
import com.stoury.domain.Like;
import com.stoury.domain.Member;
import com.stoury.exception.AlreadyLikedFeedException;
import com.stoury.repository.LikeRepository;
import com.stoury.validator.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final Validator validator;

    @Transactional
    public void like(Member liker, Feed feed) {
        validator.isMemberExists(liker);
        validator.isFeedExists(feed);

        if (likeRepository.existsByMemberAndFeed(liker, feed)) {
            throw new AlreadyLikedFeedException("You already liked the feed");
        }

        Like like = new Like(liker, feed);
        likeRepository.save(like);
    }

    @Transactional
    public void likeCancel(Member canceler, Feed feed) {
        validator.isMemberExists(canceler);
        validator.isFeedExists(feed);

        likeRepository.deleteByMemberAndFeed(canceler, feed);
    }
}
