package com.stoury.service;

import com.stoury.domain.Feed;
import com.stoury.domain.Like;
import com.stoury.domain.Member;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.LikeRepository;
import com.stoury.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LikeServiceTest {
    @Mock
    MemberRepository memberRepository;
    @Mock
    FeedRepository feedRepository;
    @Mock
    LikeRepository likeRepository;
    @InjectMocks
    LikeService likeService;

    @Test
    @DisplayName("좋아요 성공")
    void likeSuccess() {
        when(memberRepository.existsById(any())).thenReturn(true);
        when(feedRepository.existsById(any())).thenReturn(true);
        when(likeRepository.existsByMemberAndFeed(any(Member.class), any(Feed.class))).thenReturn(false);

        Member dummyLiker = Member.builder().build();
        Feed dummyFeed = Feed.builder().build();

        likeService.like(dummyLiker, dummyFeed);

        verify(likeRepository, times(1)).save(any(Like.class));
    }
}
