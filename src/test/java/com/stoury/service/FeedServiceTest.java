package com.stoury.service;

import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import com.stoury.domain.Tag;
import com.stoury.dto.FeedCreateRequest;
import com.stoury.dto.FeedResponse;
import com.stoury.event.GraphicSaveEvent;
import com.stoury.exception.FeedCreateException;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.MemberRepository;
import com.stoury.repository.TagRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeedServiceTest {
    @Mock
    MemberRepository memberRepository;
    @Mock
    TagRepository tagRepository;
    @Mock
    FeedRepository feedRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @InjectMocks
    FeedService feedService;
    Member writer;

    @BeforeEach
    @AfterEach
    void tearDown() {
        feedRepository.deleteAll();
        memberRepository.deleteAll();
        tagRepository.deleteAll();
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
        Member member = Member.builder()
                .username("writer")
                .encryptedPassword("dqwdasda")
                .email("d1wd@wdwfef.com")
                .build();
        Member writer = memberRepository.save(member);

        List<MultipartFile> graphicContents = List.of(
                new MockMultipartFile("Files", "first", "image/jpeg", new byte[0]),
                new MockMultipartFile("Files", "second", "video/mp4", new byte[0])
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

        verify(eventPublisher, times(graphicContents.size())).publishEvent(any(GraphicSaveEvent.class));
    }

    @Test
    @DisplayName("피드 생성과 동시에 태그 생성 성공")
    void createFeedWithTagsSuccess() {
        // 태그가 없는 상태에서 시작
        assertThat(tagRepository.findAll()).isEmpty();

        Member member = Member.builder()
                .username("writer")
                .encryptedPassword("dqwdasda")
                .email("d1wd@wdwfef.com")
                .build();
        Member writer = memberRepository.save(member);

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
        Member member = Member.builder()
                .username("writer")
                .encryptedPassword("dqwdasda")
                .email("d1wd@wdwfef.com")
                .build();
        Member writer = memberRepository.save(member);

        List<String> tagGroup = tagRepository.saveAll(List.of(
                new Tag("tag1"),
                new Tag("tag2"),
                new Tag("tag3"))).stream().map(Tag::getTagName).toList();

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
        Member member = Member.builder()
                .username("writer")
                .encryptedPassword("dqwdasda")
                .email("d1wd@wdwfef.com")
                .build();
        Member writer = memberRepository.save(member);

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
        Member member = Member.builder()
                .username("writer")
                .encryptedPassword("dqwdasda")
                .email("d1wd@wdwfef.com")
                .build();
        Member writer = memberRepository.save(member);

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

    @Test
    @DisplayName("태그로 피드 조회")
    @Transactional
    void searchFeedsByTag() {
        String tagName = "testTag";
        Tag tag = new Tag(tagName);
        tagRepository.save(tag);
        List<Feed> feeds = create20FeedsWith(tag);
        feedRepository.saveAll(feeds);

        LocalDateTime maxDateTime = LocalDate.of(2100, 12, 31).atStartOfDay();
        Slice<FeedResponse> recent10Feeds = feedService.getFeedsByTag(tagName, maxDateTime);

        assertThat(recent10Feeds).hasSize(10);
        assertThat(recent10Feeds.hasNext()).isTrue();

        List<FeedResponse> recentFeedResponses = recent10Feeds.getContent();
        for (int i = 0; i < recentFeedResponses.size(); i++) {
            FeedResponse feedResponse = recentFeedResponses.get(i);
            assertThat(feedResponse.textContent()).isEqualTo(feeds.get(feeds.size() - i - 1).getTextContent());
        }
    }

    @Test
    @DisplayName("사용자 피드 조회")
    void searchFeedsOfMember() {
        Member member = Member.builder()
                .username("writer")
                .encryptedPassword("dqwdasda")
                .email("d1wd@wdwfef.com")
                .build();
        Member writer = memberRepository.save(member);

        List<Feed> feeds = IntStream.range(0, 20).mapToObj(i -> Feed.builder()
                .textContent("feed" + i)
                .latitude(11.11)
                .longitude(11.11)
                .member(writer)
                .build()).toList();
        feedRepository.saveAll(feeds);

        LocalDateTime maxDateTime = LocalDate.of(2100, 12, 31).atStartOfDay();
        Slice<FeedResponse> recent10Feeds = feedService.getFeedsOfMemberId(writer.getId(), maxDateTime);

        List<String> feedTexts = recent10Feeds.stream().map(FeedResponse::textContent).toList();
        assertThat(recent10Feeds.hasNext()).isTrue();
        assertThat(feedTexts).containsExactly(
                "feed19", "feed18", "feed17", "feed16", "feed15",
                "feed14", "feed13", "feed12", "feed11", "feed10"
        );
    }

    private List<Feed> create20FeedsWith(Tag tag) {
        Member member = Member.builder()
                .username("writer")
                .encryptedPassword("dqwdasda")
                .email("d1wd@wdwfef.com")
                .build();
        Member writer = memberRepository.save(member);

        List<Feed> feeds = new ArrayList<>();

        List<Tag> tagList = List.of(tag);
        for (int i = 0; i < 20; i++) {
            Feed feed = Feed.builder()
                    .member(writer)
                    .textContent("#" + i)
                    .tags(tagList)
                    .longitude(11.11)
                    .latitude(11.11)
                    .build();
            feeds.add(feed);
        }
        return feeds;
    }
}
