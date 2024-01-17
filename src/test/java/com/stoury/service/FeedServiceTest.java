package com.stoury.service;

import com.stoury.domain.Member;
import com.stoury.dto.FeedCreateRequest;
import com.stoury.dto.FeedResponse;
import com.stoury.event.FileSaveEvent;
import com.stoury.exception.FeedCreateException;
import com.stoury.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@RecordApplicationEvents
public class FeedServiceTest {
    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    ApplicationEvents events;
    @Autowired
    FeedService feedService;
    @Autowired
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

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("피드 생성 성공")
    void createFeedSuccess() {
        FeedCreateRequest createFeed = FeedCreateRequest.builder()
                .textContent("testing")
                .longitude(111.111)
                .latitude(333.333)
                .build();
        List<MultipartFile> graphicContents = List.of(
                new MockMultipartFile("First", new byte[0]),
                new MockMultipartFile("Second", new byte[0]),
                new MockMultipartFile("Third", new byte[0])
        );

        FeedResponse createdFeed = feedService.createFeed(writer, createFeed, graphicContents);

        assertThat(createdFeed.memberResponse().id()).isEqualTo(writer.getId());
        assertThat(createdFeed.memberResponse().username()).isEqualTo(writer.getUsername());
        assertThat(createdFeed.graphicContentsPaths()).hasSize(3);
        assertThat(createdFeed.longitude()).isEqualTo(111.111);
        assertThat(createdFeed.latitude()).isEqualTo(333.333);
        assertThat(createdFeed.createdAt()).isNotNull();

        long eventCount = events.stream(FileSaveEvent.class).count();
        assertThat(eventCount).isEqualTo(graphicContents.size());
    }

    @Test
    @DisplayName("피드 저장 실패, 이미지 없음")
    @Transactional
    void createFeedFailNoGraphicContents() {
        FeedCreateRequest createFeed = FeedCreateRequest.builder()
                .textContent("testing")
                .longitude(111.111)
                .latitude(333.333)
                .build();
        List<MultipartFile> graphicContents = Collections.emptyList();

        assertThatThrownBy(()->feedService.createFeed(writer, createFeed, graphicContents))
                .isInstanceOf(FeedCreateException.class);
    }

    @Test
    @DisplayName("피드 저장 실패, 위치정보 없음")
    @Transactional
    void createFeedFailNoCoordinate() {
        FeedCreateRequest createFeed = FeedCreateRequest.builder()
                .textContent("testing")
                .build();
        List<MultipartFile> graphicContents = List.of(
                new MockMultipartFile("First", new byte[0]),
                new MockMultipartFile("Second", new byte[0]),
                new MockMultipartFile("Third", new byte[0])
        );

        assertThatThrownBy(()->feedService.createFeed(writer, createFeed, graphicContents))
                .isInstanceOf(FeedCreateException.class);
    }
}
