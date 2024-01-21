package com.stoury.service;

import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import com.stoury.domain.Tag;
import com.stoury.dto.FeedCreateRequest;
import com.stoury.dto.FeedResponse;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.MemberRepository;
import com.stoury.repository.TagRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Slice;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class FeedServiceIntegrationTest {
    Member writer;
    List<MultipartFile> graphicContents = List.of(
            new MockMultipartFile("Files", "first.jpeg", "image/jpeg", new byte[0]),
            new MockMultipartFile("Files", "second.mp4", "video/mp4", new byte[0])
    );
    @Autowired
    FeedService feedService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TagRepository tagRepository;
    @Autowired
    FeedRepository feedRepository;

    @BeforeEach
    void setup() {
        feedRepository.deleteAll();
        memberRepository.deleteAll();
        tagRepository.deleteAll();

        writer = memberRepository.save(Member.builder()
                .username("writer")
                .encryptedPassword("dqwdasda")
                .email("d1wd@wdwfef.com")
                .build());
    }

    @AfterEach
    void tearDown() {
        feedRepository.deleteAll();
        memberRepository.deleteAll();
        tagRepository.deleteAll();
    }

    @Test
    @DisplayName("피드 생성과 동시에 태그 생성 성공")
    void createFeedWithTagsSuccess() {
        List<String> tagGroup = List.of("tag1", "tag2", "tag3");
        FeedCreateRequest feedCreateRequest = FeedCreateRequest.builder()
                .textContent("with tag group1")
                .longitude(11.11)
                .latitude(11.11)
                .tagNames(tagGroup)
                .build();

        FeedResponse feedResponse = feedService.createFeed(writer, feedCreateRequest, graphicContents);

        assertThat(feedResponse.tagNames()).containsExactly(tagGroup.toArray(String[]::new));
    }


    @Test
    @DisplayName("이미 존재하는 태그로 피드 생성 성공")
    void createFeedWithExistTags() {
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

        FeedResponse feedResponse = feedService.createFeed(writer, feedCreateRequest, graphicContents);

        assertThat(feedResponse.tagNames()).containsExactly(tagGroup.toArray(String[]::new));
    }

    @Test
    @DisplayName("태그로 피드 조회")
    @Transactional
    void searchFeedsByTag() {
        String tagName = "testTag";
        Tag tag = new Tag(tagName);
        tagRepository.save(tag);
        List<Feed> feeds = create20FeedsWith(List.of(tag));
        feedRepository.saveAll(feeds);

        LocalDateTime maxDateTime = LocalDate.of(2100, 12, 31).atStartOfDay();
        Slice<FeedResponse> feedSlice = feedService.getFeedsOfMemberId(writer.getId(), maxDateTime);

        assertThat(feedSlice.hasNext()).isTrue();
        List<FeedResponse> feedList = feedSlice.getContent();

        for (int i = 0; i < feedList.size() - 1; i++) {
            FeedResponse recentFeed = feedList.get(i);
            FeedResponse olderFeed = feedList.get(i + 1);

            assertThat(recentFeed.createdAt().isAfter(olderFeed.createdAt())).isTrue();
        }
    }

    @Test
    @DisplayName("사용자 피드 조회")
    void searchFeedsOfMember() {
        List<Feed> feeds = create20FeedsWith(Collections.emptyList());
        feedRepository.saveAll(feeds);

        LocalDateTime maxDateTime = LocalDate.of(2100, 12, 31).atStartOfDay();
        Slice<FeedResponse> feedSlice = feedService.getFeedsOfMemberId(writer.getId(), maxDateTime);

        assertThat(feedSlice.hasNext()).isTrue();
        List<FeedResponse> feedList = feedSlice.getContent();

        for (int i = 0; i < feedList.size() - 1; i++) {
            FeedResponse recentFeed = feedList.get(i);
            FeedResponse olderFeed = feedList.get(i + 1);

            assertThat(recentFeed.createdAt().isAfter(olderFeed.createdAt())).isTrue();
        }
    }

    private List<Feed> create20FeedsWith(List<Tag> tags) {
        List<Feed> feeds = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            Feed feed = Feed.builder()
                    .member(writer)
                    .textContent("feed" + i)
                    .tags(tags)
                    .longitude(11.11)
                    .latitude(11.11)
                    .build();
            feeds.add(feed);
        }
        return feeds;
    }
}
