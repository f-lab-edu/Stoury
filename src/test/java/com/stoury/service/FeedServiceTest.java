package com.stoury.service;

import com.stoury.domain.Member;
import com.stoury.dto.FeedCreateRequest;
import com.stoury.dto.FeedResponse;
import com.stoury.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class FeedServiceTest {
    FeedService feedService;
    MemberRepository memberRepository;
    Member writer;

    @BeforeEach
    void setup() {
        Member member = Member.builder()
                .username("writer")
                .encryptedPassword("dqwdasda")
                .email("d1wd@wdwfef.com")
                .build();
        writer = memberRepository.save(member);
    }

    @Test
    @DisplayName("피드 생성 성공")
    void createFeedSuccess() {
        FeedCreateRequest createFeed = FeedCreateRequest.builder()
                .writerId(writer.getId())
                .textContent("testing")
                .longitude(111.111)
                .latitude(333.333)
                .build();
        List<MultipartFile> graphicContents = List.of(
                new MockMultipartFile("First", new byte[0]),
                new MockMultipartFile("Second", new byte[0]),
                new MockMultipartFile("Third", new byte[0])
                );

        FeedResponse createdFeed = feedService.createFeed(createFeed, graphicContents);

        Assertions.assertThat(createdFeed.memberResponse().id()).isEqualTo(writer.getId());
        Assertions.assertThat(createdFeed.memberResponse().username()).isEqualTo(writer.getUsername());
        Assertions.assertThat(createdFeed.graphicContentsPaths()).hasSize(3);
        Assertions.assertThat(createdFeed.longitude()).isEqualTo(111.111);
        Assertions.assertThat(createdFeed.latitude()).isEqualTo(333.333);
    }
}
