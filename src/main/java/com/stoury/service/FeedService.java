package com.stoury.service;

import com.stoury.domain.Feed;
import com.stoury.domain.GraphicContent;
import com.stoury.domain.Member;
import com.stoury.domain.Tag;
import com.stoury.dto.feed.FeedCreateRequest;
import com.stoury.dto.feed.FeedResponse;
import com.stoury.dto.feed.FeedUpdateRequest;
import com.stoury.event.GraphicDeleteEvent;
import com.stoury.event.GraphicSaveEvent;
import com.stoury.exception.feed.FeedCreateException;
import com.stoury.exception.feed.FeedSearchException;
import com.stoury.exception.feed.FeedUpdateException;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.LikeRepository;
import com.stoury.repository.MemberRepository;
import com.stoury.utils.FileUtils;
import com.stoury.utils.SupportedFileType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FeedService {
    @Value("${path-prefix}")
    public  String pathPrefix;
    public static final int PAGE_SIZE = 10;
    private final FeedRepository feedRepository;
    private final MemberRepository memberRepository;
    private final LikeRepository likeRepository;
    private final TagService tagService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FeedResponse createFeed(Member writer, FeedCreateRequest feedCreateRequest,
                                   List<MultipartFile> graphicContentsFiles) {
        validate(writer, feedCreateRequest, graphicContentsFiles);

        List<GraphicContent> graphicContents = saveGraphicContents(graphicContentsFiles);

        Feed feedEntity = createFeedEntity(writer, feedCreateRequest, graphicContents);

        Feed uploadedFeed = feedRepository.save(feedEntity);

        return FeedResponse.from(uploadedFeed, 0);
    }

    private List<GraphicContent> saveGraphicContents(List<MultipartFile> graphicContents) {
        List<GraphicContent> graphicContentList = new ArrayList<>();

        for (int i = 0; i < graphicContents.size(); i++) {
            MultipartFile file = graphicContents.get(i);

            GraphicSaveEvent event = publishNewFileEvent(file);
            String contentPath = event.getPath();

            graphicContentList.add(new GraphicContent(contentPath, i));
        }

        return graphicContentList;
    }

    private Feed createFeedEntity(Member writer, FeedCreateRequest feedCreateRequest, List<GraphicContent> graphicContents) {
        List<Tag> tags = getOrCreateTags(feedCreateRequest.tagNames());
        return feedCreateRequest.toEntity(writer, graphicContents, tags);
    }

    private List<Tag> getOrCreateTags(List<String> tagNames) {
        return tagNames.stream()
                .map(tagService::getTagOrElseCreate)
                .toList();
    }

    private GraphicSaveEvent publishNewFileEvent(MultipartFile file) {
        String path = FileUtils.createFilePath(file, pathPrefix);
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
    public List<FeedResponse> getFeedsOfMemberId(Long memberId, LocalDateTime orderThan) {
        Member feedWriter = memberRepository.findById(Objects.requireNonNull(memberId))
                .orElseThrow(() -> new FeedCreateException("Cannot find the member."));

        Pageable page = PageRequest.of(0, PAGE_SIZE, Sort.by("createdAt").descending());
        List<Feed> feeds = feedRepository.findAllByMemberAndCreatedAtIsBefore(feedWriter, orderThan, page);

        return feeds.stream().map(feed -> FeedResponse.from(feed, likeRepository.countByFeed(feed))).toList();
    }

    @Transactional(readOnly = true)
    public List<FeedResponse> getFeedsByTag(String tagName, LocalDateTime orderThan) {
        Pageable page = PageRequest.of(0, PAGE_SIZE, Sort.by("createdAt").descending());

        List<Feed> feeds = feedRepository.findByTagAndCreateAtLessThan(tagName, orderThan, page);

        return feeds.stream().map(feed -> FeedResponse.from(feed, likeRepository.countByFeed(feed))).toList();
    }

    @Transactional
    public FeedResponse updateFeed(Long feedId, Member writer, FeedUpdateRequest feedUpdateRequest) {
        Feed feed = getFeed(feedId);

        if (!feed.getMember().equals(writer)) {
            throw new FeedUpdateException("Not Authorized");
        }

        List<GraphicContent> beforeDeleteGraphicContents = new ArrayList<>(feed.getGraphicContents());

        feed.update(feedUpdateRequest, getOrCreateTags(feedUpdateRequest.tagNames()));

        publishDeleteFileEvents(beforeDeleteGraphicContents, feed.getGraphicContents());

        return FeedResponse.from(feed, likeRepository.countByFeed(feed));
    }

    private void publishDeleteFileEvents(List<GraphicContent> beforeDeleteGraphicContents,
                                         List<GraphicContent> afterDeleteGraphicContents) {
        for (GraphicContent beforeDeleteGraphicContent : beforeDeleteGraphicContents) {
            if (!afterDeleteGraphicContents.contains(beforeDeleteGraphicContent)) {
                eventPublisher.publishEvent(new GraphicDeleteEvent(this, beforeDeleteGraphicContent.getPath()));
            }
        }
    }

    @Transactional
    public void deleteFeed(Feed feed) {
        feedRepository.delete(feed);
    }

    @Transactional(readOnly = true)
    public Feed getFeed(Long feedId) {
        return feedRepository.findById(Objects.requireNonNull(feedId))
                .orElseThrow(FeedSearchException::new);
    }
}
