package com.stoury.service;

import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import com.stoury.dto.FeedCreateRequest;
import com.stoury.dto.FeedResponse;
import com.stoury.event.FileSaveEvent;
import com.stoury.exception.FeedCreateException;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeedServiceTest {
    @Mock
    MemberRepository memberRepository;
    @Mock
    FeedRepository feedRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @InjectMocks
    FeedService feedService;
    Member writer;

    @BeforeEach
    void setup() {
        writer = Member.builder()
                .username("writer")
                .encryptedPassword("dqwdasda")
                .email("d1wd@wdwfef.com")
                .build();
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("피드 생성 성공")
    void createFeedSuccess() {
        when(memberRepository.existsById(any())).thenReturn(true);
        FeedCreateRequest feedCreateRequest = FeedCreateRequest.builder()
                .textContent("testing")
                .longitude(111.111)
                .latitude(333.333)
                .build();
        List<MultipartFile> graphicContents = List.of(
                new MockMultipartFile("First", new byte[0]),
                new MockMultipartFile("Second", new byte[0]),
                new MockMultipartFile("Third", new byte[0])
        );
        when(feedRepository.save(any(Feed.class))).thenReturn(Feed.builder()
                .member(writer)
                .textContent(feedCreateRequest.textContent())
                .longitude(feedCreateRequest.longitude())
                .latitude(feedCreateRequest.latitude())
                .build());

        FeedResponse createdFeed = feedService.createFeed(writer, feedCreateRequest, graphicContents);

        assertThat(createdFeed.memberResponse().id()).isEqualTo(writer.getId());
        assertThat(createdFeed.memberResponse().username()).isEqualTo(writer.getUsername());
        assertThat(createdFeed.longitude()).isEqualTo(feedCreateRequest.longitude());
        assertThat(createdFeed.latitude()).isEqualTo(feedCreateRequest.latitude());

        verify(eventPublisher, times(graphicContents.size())).publishEvent(any(FileSaveEvent.class));
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
