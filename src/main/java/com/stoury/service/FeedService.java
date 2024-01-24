package com.stoury.service;

import com.stoury.domain.Feed;
import com.stoury.domain.GraphicContent;
import com.stoury.domain.Member;
import com.stoury.domain.Tag;
import com.stoury.dto.FeedCreateRequest;
import com.stoury.dto.FeedResponse;
import com.stoury.event.GraphicSaveEvent;
import com.stoury.exception.FeedCreateException;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.MemberRepository;
import com.stoury.utils.FileUtils;
import com.stoury.utils.SupportedFileType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class FeedService {
    public static final String PATH_PREFIX = "/feeds";
    public static final int PAGE_SIZE = 10;
    private final FeedRepository feedRepository;
    private final MemberRepository memberRepository;
    private final TagService tagService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FeedResponse createFeed(Member writer, FeedCreateRequest feedCreateRequest,
                                   List<MultipartFile> graphicContentsFiles) {
        validate(writer, feedCreateRequest, graphicContentsFiles);

        List<GraphicContent> graphicContents = saveGraphicContents(graphicContentsFiles);

        Feed feedEntity = createFeedEntity(writer, feedCreateRequest, graphicContents);

        Feed uploadedFeed = feedRepository.save(feedEntity);

        return FeedResponse.from(uploadedFeed);
    }

    private List<GraphicContent> saveGraphicContents(List<MultipartFile> graphicContents) {
        List<GraphicContent> graphicContentList = new ArrayList<>();

        for (int i = 0; i < graphicContents.size(); i++) {
            MultipartFile file = graphicContents.get(i);

            GraphicSaveEvent event = publishEventFrom(file);
            String contentPath = event.getPath();

            graphicContentList.add(new GraphicContent(contentPath, i));
        }

        return graphicContentList;
    }

    private Feed createFeedEntity(Member writer, FeedCreateRequest feedCreateRequest, List<GraphicContent> graphicContents) {
        List<Tag> tags = feedCreateRequest.tagNames().stream()
                .map(tagService::getTagOrElseCreate)
                .toList();
        return feedCreateRequest.toEntity(writer, graphicContents, tags);
    }

    private GraphicSaveEvent publishEventFrom(MultipartFile file) {
        String path = FileUtils.createFilePath(file, PATH_PREFIX);
        GraphicSaveEvent event = new GraphicSaveEvent(this, file, path);
        eventPublisher.publishEvent(event);
        return event;
    }

    private void validate(Member writer, FeedCreateRequest feedCreateRequest, List<MultipartFile> graphicContents) {
        if (!memberRepository.existsById(writer.getId())) {
            throw new FeedCreateException("Cannot find the member.");
        }
        if (graphicContents.isEmpty() || graphicContents.stream().anyMatch(SupportedFileType::isUnsupportedFile)) {
            throw new FeedCreateException("Input files are empty or unsupported.");
        }
        if (feedCreateRequest.longitude() == null || feedCreateRequest.latitude() == null) {
            throw new FeedCreateException("Location information is required.");
        }
    }

    @Transactional(readOnly = true)
    public Slice<FeedResponse> getFeedsOfMemberId(Long memberId, LocalDateTime orderThan) {
        Member feedWriter = memberRepository.findById(Objects.requireNonNull(memberId))
                .orElseThrow(() -> new FeedCreateException("Cannot find the member."));

        Pageable page = PageRequest.of(0, PAGE_SIZE, Sort.by("createdAt").descending());
        Slice<Feed> feedSlice = feedRepository.findAllByMemberAndCreatedAtIsBefore(feedWriter, orderThan, page);

        return FeedResponse.from(feedSlice);
    }

    @Transactional(readOnly = true)
    public Slice<FeedResponse> getFeedsByTag(String tagName, LocalDateTime orderThan) {
        Pageable page = PageRequest.of(0, PAGE_SIZE, Sort.by("createdAt").descending());

        Slice<Feed> feedSlice = feedRepository.findByTagAndCreateAtLessThan(tagName, orderThan, page);

        return FeedResponse.from(feedSlice);
    }
}
