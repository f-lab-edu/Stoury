package com.stoury.service;

import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.LikeRepository;
import com.stoury.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@ActiveProfiles("test")
public class LikeServiceIntegrationTest {
    @Autowired
    LikeService likeService;
    @Autowired
    LikeRepository likeRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    FeedRepository feedRepository;

    Member writer;
    Feed feed;

    @BeforeEach
    void setup() {
        writer = memberRepository.save(Member.builder()
                .email("aaa@aaaa.com")
                .username("writer")
                .encryptedPassword("123123123")
                .build());
        feed = feedRepository.save(Feed.builder()
                .member(writer)
                .latitude(0.0)
                .longitude(0.0)
                .textContent("blablabla")
                .build());
    }

    @AfterEach
    void tearDown() {
        likeRepository.deleteAll();
        feedRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("좋아요 수 0 -> 1 증가")
    void increaseLike() {
        Member liker = memberRepository.save(Member.builder()
                .username("liker")
                .email("1111@asasas.com")
                .encryptedPassword("321321312")
                .build());
        likeService.like(liker, feed);

        Optional<Integer> likeCount = likeRepository.countByFeed(feed);
        assertThat(likeCount).isPresent();
        assertThat(likeCount.get()).isOne();
    }
}
