package com.stoury.service;

import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import com.stoury.dto.FeedCreateRequest;
import com.stoury.dto.FeedResponse;
import com.stoury.event.GraphicSaveEvent;
import com.stoury.exception.FeedCreateException;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.MemberRepository;
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

import java.util.ArrayList;
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
    TagService tagService;
    @Mock
    FeedRepository feedRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @InjectMocks
    FeedService feedService;
    Member writer;

    @Test
    @DisplayName("피드 생성 성공")
    void createFeedSuccess() {
        when(memberRepository.existsById(any())).thenReturn(true);
        FeedCreateRequest feedCreateRequest = FeedCreateRequest.builder()
                .textContent("testing")
                .longitude(111.111)
                .latitude(333.333)
                .build();
        writer = Member.builder()
                .username("writer")
                .encryptedPassword("dqwdasda")
                .email("d1wd@wdwfef.com")
                .build();

        List<MultipartFile> graphicContents = List.of(
                new MockMultipartFile("Files", "first", "image/jpeg", new byte[0]),
                new MockMultipartFile("Files", "second", "video/mp4", new byte[0])
        );
        when(feedRepository.save(any(Feed.class))).thenReturn(Feed.builder()
                .member(writer)
                .textContent(feedCreateRequest.textContent())
                .longitude(feedCreateRequest.longitude())
                .latitude(feedCreateRequest.latitude())
                .tags(new ArrayList<>())
                .build());

        FeedResponse createdFeed = feedService.createFeed(writer, feedCreateRequest, graphicContents);

        assertThat(createdFeed.memberResponse().id()).isEqualTo(writer.getId());
        assertThat(createdFeed.memberResponse().username()).isEqualTo(writer.getUsername());
        assertThat(createdFeed.longitude()).isEqualTo(feedCreateRequest.longitude());
        assertThat(createdFeed.latitude()).isEqualTo(feedCreateRequest.latitude());

        verify(eventPublisher, times(graphicContents.size())).publishEvent(any(GraphicSaveEvent.class));
    }

    @Test
    @DisplayName("피드 생성과 동시에 태그 생성 성공")
    void createFeedWithTagsSuccess() {
        when(memberRepository.existsById(any())).thenReturn(true);
        writer = Member.builder()
                .username("writer")
                .encryptedPassword("dqwdasda")
                .email("d1wd@wdwfef.com")
                .build();

        List<String> tagGroup = List.of("tag1", "tag2", "tag3");
        FeedCreateRequest feedCreateRequest = FeedCreateRequest.builder()
                .textContent("with tag group1")
                .longitude(11.11)
                .latitude(11.11)
                .tagNames(tagGroup)
                .build();
        List<MultipartFile> graphicContents = List.of(
                new MockMultipartFile("Files", "first", "image/jpeg", new byte[0]),
                new MockMultipartFile("Files", "second", "video/mp4", new byte[0])
        );

        FeedResponse feedResponse = feedService.createFeed(writer, feedCreateRequest, graphicContents);

        assertThat(feedResponse.tagNames()).containsExactly(tagGroup.toArray(String[]::new));
    }

    @Test
    @DisplayName("이미 존재하는 태그로 피드 생성 성공")
    void createFeedWithExistTags() {
        when(memberRepository.existsById(any())).thenReturn(true);
        writer = Member.builder()
                .username("writer")
                .encryptedPassword("dqwdasda")
                .email("d1wd@wdwfef.com")
                .build();

        List<String> tagGroup = List.of("tag1", "tag2", "tag3");

        FeedCreateRequest feedCreateRequest = FeedCreateRequest.builder()
                .textContent("with tag group1")
                .longitude(11.11)
                .latitude(11.11)
                .tagNames(tagGroup)
                .build();
        List<MultipartFile> graphicContents = List.of(
                new MockMultipartFile("Files", "first", "image/jpeg", new byte[0]),
                new MockMultipartFile("Files", "second", "video/mp4", new byte[0])
        );

        FeedResponse feedResponse = feedService.createFeed(writer, feedCreateRequest, graphicContents);

        assertThat(feedResponse.tagNames()).containsExactly(tagGroup.toArray(String[]::new));
    }

    @Test
    @DisplayName("피드 생성 실패, 지원하지 않는 파일")
    @Transactional
    void createFeedFailByNotSupportedFileFormat() {
        when(memberRepository.existsById(any())).thenReturn(true);
        writer = Member.builder()
                .username("writer")
                .encryptedPassword("dqwdasda")
                .email("d1wd@wdwfef.com")
                .build();

        FeedCreateRequest createFeed = FeedCreateRequest.builder()
                .textContent("testing")
                .longitude(111.111)
                .latitude(333.333)
                .build();
        List<MultipartFile> graphicContents = List.of(
                new MockMultipartFile("Files", "first", "image/jpeg", new byte[0]),
                new MockMultipartFile("Files", "second", "video/mp4", new byte[0]),
                new MockMultipartFile("Files", "third", "image/png", new byte[0])
        );

        assertThatThrownBy(() -> feedService.createFeed(writer, createFeed, graphicContents))
                .isInstanceOf(FeedCreateException.class);
    }

    @Test
    @DisplayName("피드 저장 실패, 이미지 없음")
    @Transactional
    void createFeedFailNoGraphicContents() {
        when(memberRepository.existsById(any())).thenReturn(true);
        writer = Member.builder()
                .username("writer")
                .encryptedPassword("dqwdasda")
                .email("d1wd@wdwfef.com")
                .build();

        FeedCreateRequest createFeed = FeedCreateRequest.builder()
                .textContent("testing")
                .longitude(111.111)
                .latitude(333.333)
                .build();
        List<MultipartFile> graphicContents = Collections.emptyList();

        assertThatThrownBy(() -> feedService.createFeed(writer, createFeed, graphicContents))
                .isInstanceOf(FeedCreateException.class);
    }

    @Test
    @DisplayName("피드 저장 실패, 위치정보 없음")
    @Transactional
    void createFeedFailNoCoordinate() {
        assertThatThrownBy(() -> FeedCreateRequest.builder()
                .textContent("testing")
                .build()).isInstanceOf(FeedCreateException.class);
    }
}
