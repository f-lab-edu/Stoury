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
import com.stoury.utils.Validator;
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
        return IntStream.range(0, graphicContents.size())
                .mapToObj(seq -> {
                    MultipartFile file = graphicContents.get(seq);
                    GraphicSaveEvent event = publishEventFrom(file);
                    return Pair.of(seq, event);
                })
                .map(sequentialEvent -> {
                    Integer seq = sequentialEvent.getFirst();
                    String path = sequentialEvent.getSecond().getPath();
                    return new GraphicContent(path, seq);
                })
                .toList();
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
        Validator.of(writer.getId())
                .willCheck(memberRepository::existsById)
                .ifFailThrows(FeedCreateException.class)
                .withMessage("Cannot find the member.")
                .validate();

        Validator.of(graphicContents)
                .willCheck(contents -> !contents.isEmpty())
                .willCheck(contents -> contents.stream().noneMatch(SupportedFileType::isUnsupportedFile))
                .ifFailThrows(FeedCreateException.class)
                .withMessage("Input files are empty or unsupported.")
                .validate();

        Validator.of(feedCreateRequest)
                .willCheck(request -> Objects.nonNull(request.latitude()))
                .willCheck(request -> Objects.nonNull(request.longitude()))
                .ifFailThrowsWithMessage(FeedCreateException.class, "Location information is required.")
                .validate();
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
