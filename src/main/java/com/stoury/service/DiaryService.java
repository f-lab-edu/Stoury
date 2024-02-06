package com.stoury.service;

import com.stoury.domain.Diary;
import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import com.stoury.dto.diary.DiaryCreateRequest;
import com.stoury.dto.diary.DiaryResponse;
import com.stoury.dto.feed.FeedResponse;
import com.stoury.exception.authentication.NotAuthorizedException;
import com.stoury.exception.feed.FeedSearchException;
import com.stoury.exception.member.MemberSearchException;
import com.stoury.repository.DiaryRepository;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.LikeRepository;
import com.stoury.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;
    private final DiaryRepository diaryRepository;
    private final LikeRepository likeRepository;
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @Transactional
    public DiaryResponse createDiary(DiaryCreateRequest diaryCreateRequest, Long memberId) {
        List<Long> feedIds = diaryCreateRequest.feedIds();
        if (feedIds == null || feedIds.isEmpty()) {
            throw new IllegalArgumentException("Feeds cannot be empty");
        }

        Member member = memberRepository.findById(Objects.requireNonNull(memberId, "Member Id cannot be null"))
                .orElseThrow(MemberSearchException::new);

        List<Feed> feeds = feedIds.stream()
                .map(feedRepository::findById)
                .map(feedOptional -> feedOptional.orElseThrow(FeedSearchException::new))
                .filter(feed -> validateOwnership(member, feed))
                .toList();

        String title ;
        if (StringUtils.hasText(diaryCreateRequest.title())) {
            title = diaryCreateRequest.title();
        } else {
            title = getDefaultTitle(feeds);
        }

        Diary diary = new Diary(member, feeds, title);
        Diary savedDiary = diaryRepository.save(diary);

        List<FeedResponse> feedResponses = feeds.stream()
                .map(feed -> FeedResponse.from(feed, likeRepository.getLikes(feed.getId().toString())))
                .toList();

        return DiaryResponse.from(savedDiary, feedResponses);
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
}
