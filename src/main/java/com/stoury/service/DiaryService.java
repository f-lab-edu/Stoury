package com.stoury.service;

import com.stoury.domain.Diary;
import com.stoury.domain.Feed;
import com.stoury.domain.GraphicContent;
import com.stoury.domain.Member;
import com.stoury.dto.diary.DiaryCreateRequest;
import com.stoury.dto.diary.DiaryPageResponse;
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
import com.stoury.utils.PageSize;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static com.stoury.utils.Values.MEMBER_ID_NOT_NULL_MESSAGE;

@Service
@RequiredArgsConstructor
public class DiaryService {
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;
    private final DiaryRepository diaryRepository;
    private final LikeRepository likeRepository;

    @Transactional
    public DiaryResponse createDiary(DiaryCreateRequest diaryCreateRequest, Long memberId) {
        List<Long> feedIds = diaryCreateRequest.feedIds();
        validateFeedIds(feedIds);
        validateMemberId(memberId);

        Member member = memberRepository.findById(memberId).orElseThrow(MemberSearchException::new);

        List<Feed> feeds = getFeeds(feedIds);

        feeds.forEach(feed -> validateOwnership(member, feed));

        GraphicContent thumbnail = getThumbnail(feeds, diaryCreateRequest.thumbnailId());
        String title = getTitle(diaryCreateRequest, feeds);

        Diary savedDiary = diaryRepository.save(new Diary(member, feeds, title, thumbnail));

        return toDiaryResponse(savedDiary);
    }

    private GraphicContent getThumbnail(List<Feed> feeds, Long thumbnailId) {
        return feeds.stream()
                .flatMap(feed -> feed.getGraphicContents().stream())
                .filter(GraphicContent::isImage)
                .filter(graphicContent -> graphicContent.getId().equals(thumbnailId))
                .findFirst()
                .orElseThrow(() -> new DiaryCreateException("Select a thumbnail image from your feed images"));
    }

    private List<Feed> getFeeds(List<Long> feedIds) {
        return feedIds.stream()
                .map(feedRepository::findById)
                .map(feedOptional -> feedOptional.orElseThrow(FeedSearchException::new))
                .toList();
    }

    private void validateFeedIds(List<Long> feedIds) {
        if (feedIds == null || feedIds.isEmpty()) {
            throw new DiaryCreateException("Feeds cannot be empty");
        }
    }

    private void validateMemberId(Long memberId) {
        if (memberId == null) {
            throw new DiaryCreateException(MEMBER_ID_NOT_NULL_MESSAGE);
        }
    }

    private DiaryResponse toDiaryResponse(Diary diary) {
        List<FeedResponse> feedResponses = diary.getFeeds().stream()
                .map(feed -> FeedResponse.from(feed, likeRepository.getLikes(feed.getId().toString())))
                .toList();

        return DiaryResponse.from(diary, feedResponses);
    }

    private String getTitle(DiaryCreateRequest diaryCreateRequest, List<Feed> feeds) {
        if (StringUtils.hasText(diaryCreateRequest.title())) {
            return diaryCreateRequest.title();
        }
        return getDefaultTitle(feeds);
    }

    private String getDefaultTitle(List<Feed> sortedFeeds) {
        Feed firstFeed = sortedFeeds.get(0);
        Feed lastFeed = sortedFeeds.get(sortedFeeds.size() - 1);
        String country = firstFeed.getCountry();
        String city = firstFeed.getCity();
        String startDate = dateFormatter.format(firstFeed.getCreatedAt());
        String lastDate = dateFormatter.format(lastFeed.getCreatedAt());

        return "%s, %s, %s~%s".formatted(country, city, startDate, lastDate);
    }

    private void validateOwnership(Member member, Feed feed) {
        if (feed.notOwnedBy(member)) {
            throw new NotAuthorizedException("Not your feed");
        }
    }

    @Transactional(readOnly = true)
    public DiaryPageResponse getMemberDiaries(Long memberId, Long offsetId) {
        Member member = memberRepository.findById(Objects.requireNonNull(memberId, MEMBER_ID_NOT_NULL_MESSAGE))
                .orElseThrow(MemberSearchException::new);
        Long offsetIdNotNull = Objects.requireNonNull(offsetId);

        Pageable page = PageRequest.of(0, PageSize.DIARY_PAGE_SIZE, Sort.by("createdAt"));

        List<Diary> diaryPage = diaryRepository.findByMemberAndIdLessThan(member, offsetIdNotNull, page);

        return DiaryPageResponse.from(diaryPage);
    }

    protected SimpleDiaryResponse cancelDiary(Long diaryId) {
        Diary toCancelDiary = diaryRepository.findById(diaryId).orElseThrow(DiarySearchException::new);

        diaryRepository.delete(toCancelDiary);

        return SimpleDiaryResponse.from(toCancelDiary);
    }

    @Transactional
    public void cancelDiaryIfOwner(Long diaryId, Long memberId) {
        Diary toCancelDiary = diaryRepository.findById(diaryId).orElseThrow(DiarySearchException::new);

        if (toCancelDiary.notOwnedBy(memberId)) {
            throw new NotAuthorizedException();
        }

        cancelDiary(diaryId);
    }

    @Transactional(readOnly = true)
    public DiaryResponse getDiary(Long diaryId) {
        Long diaryIdNotNull = Objects.requireNonNull(diaryId, "Diary id cannot be null");

        return diaryRepository.findById(diaryIdNotNull)
                .map(this::toDiaryResponse)
                .orElseThrow(DiarySearchException::new);
    }
}
