package com.stoury.service;

import com.stoury.domain.Diary;
import com.stoury.domain.Feed;
import com.stoury.domain.GraphicContent;
import com.stoury.domain.Member;
import com.stoury.dto.diary.DiaryCreateRequest;
import com.stoury.dto.diary.DiaryResponse;
import com.stoury.dto.diary.SimpleDiaryResponse;
import com.stoury.dto.feed.FeedResponse;
import com.stoury.exception.authentication.NotAuthorizedException;
import com.stoury.exception.diary.DiaryCreateException;
import com.stoury.exception.diary.DiarySearchException;
import com.stoury.exception.feed.FeedSearchException;
import com.stoury.exception.member.MemberSearchException;
import com.stoury.repository.DiaryRepository;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.LikeRepository;
import com.stoury.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DiaryService {
    public static final int PAGE_SIZE = 10;
    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;
    private final DiaryRepository diaryRepository;
    private final LikeRepository likeRepository;
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @Transactional
    public DiaryResponse createDiary(DiaryCreateRequest diaryCreateRequest, Long memberId) {
        List<Long> feedIds = diaryCreateRequest.feedIds();
        if (feedIds == null || feedIds.isEmpty()) {
            throw new DiaryCreateException("Feeds cannot be empty");
        }

        Member member = memberRepository.findById(Objects.requireNonNull(memberId, "Member Id cannot be null"))
                .orElseThrow(MemberSearchException::new);

        List<Feed> feeds = feedIds.stream()
                .map(feedRepository::findById)
                .map(feedOptional -> feedOptional.orElseThrow(FeedSearchException::new))
                .filter(feed -> validateOwnership(member, feed))
                .toList();

        GraphicContent thumbnail = feeds.stream()
                .flatMap(feed -> feed.getGraphicContents().stream())
                .filter(GraphicContent::isImage)
                .filter(graphicContent -> graphicContent.getId().equals(diaryCreateRequest.thumbnailId()))
                .findFirst()
                .orElseThrow(() -> new DiaryCreateException("Select a thumbnail image from your feed images"));

        String title = getTitle(diaryCreateRequest, feeds);

        Diary diary = new Diary(member, feeds, title, thumbnail);
        Diary savedDiary = diaryRepository.save(diary);

        List<FeedResponse> feedResponses = feeds.stream()
                .map(feed -> FeedResponse.from(feed, likeRepository.getLikes(feed.getId().toString())))
                .toList();

        return DiaryResponse.from(savedDiary, feedResponses);
    }

    @NotNull
    private String getTitle(DiaryCreateRequest diaryCreateRequest, List<Feed> feeds) {
        String title ;
        if (StringUtils.hasText(diaryCreateRequest.title())) {
            title = diaryCreateRequest.title();
        } else {
            title = getDefaultTitle(feeds);
        }
        return title;
    }

    @NotNull
    private String getDefaultTitle(List<Feed> sortedFeeds) {
        String title;
        Feed firstFeed = sortedFeeds.get(0);
        Feed lastFeed = sortedFeeds.get(sortedFeeds.size() - 1);

        title = firstFeed.getCountry() + ", " + firstFeed.getCity() + ", "
                + dateFormatter.format(firstFeed.getCreatedAt()) + "~" + dateFormatter.format(lastFeed.getCreatedAt());
        return title;
    }

    private boolean validateOwnership(Member member, Feed feed) {
        if (!feed.getMember().equals(member)) {
            throw new NotAuthorizedException("Not your feed");
        }
        return true;
    }

    @Transactional(readOnly = true)
    public Page<SimpleDiaryResponse> getMemberDiaries(Long memberId, int pageNo) {
        Member member = memberRepository.findById(Objects.requireNonNull(memberId, "Member Id cannot be null"))
                .orElseThrow(MemberSearchException::new);
        Pageable page = PageRequest.of(pageNo, PAGE_SIZE, Sort.by("createdAt"));

        Page<Diary> diaryPage = diaryRepository.findByMember(member, page);

        return new PageImpl<>(diaryPage.map(SimpleDiaryResponse::from).toList(), diaryPage.getPageable(), diaryPage.getTotalElements());
    }

    @PostAuthorize("returnObject.memberId() == authentication.principal.id")
    @Transactional
    public SimpleDiaryResponse cancelDiary(Long diaryId) {
        Diary toCancelDiary = diaryRepository.findById(diaryId).orElseThrow(DiarySearchException::new);

        diaryRepository.delete(toCancelDiary);

        return SimpleDiaryResponse.from(toCancelDiary);
    }
}
