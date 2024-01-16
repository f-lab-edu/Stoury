package com.stoury.service;

import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import com.stoury.dto.FeedCreateRequest;
import com.stoury.dto.FeedResponse;
import com.stoury.exception.FeedCreateException;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FeedServiceTest {
    @Autowired
    ApplicationContext applicationContext;
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
    }



    @Test
    @DisplayName("피드 생성 실패, 이미지 저장 롤백")
    void createFeedFailAndRollbackGraphicContents() {
        FeedRepository mockedFeedRepository = mock(FeedRepository.class);
        when(mockedFeedRepository.save(any(Feed.class))).thenThrow(new DataAccessException("Something Failed") {});
        FileService mockedFileService = mock(FileService.class);
        FeedService failingFeedService = new FeedService(mockedFileService, mockedFeedRepository, memberRepository);

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

        assertThatThrownBy(() -> failingFeedService.createFeed(writer, createFeed, graphicContents))
                .isInstanceOf(FeedCreateException.class);

        verify(mockedFileService, atLeastOnce()).removeFiles(anyList());
    }

    @Test
    @DisplayName("피드 저장 실패, 이미지 없음")
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
}
