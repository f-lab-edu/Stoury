package com.stoury.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.stoury.domain.ClickLog;
import com.stoury.domain.Feed;
import com.stoury.domain.GraphicContent;
import com.stoury.domain.Member;
import com.stoury.domain.Tag;
import com.stoury.dto.SimpleMemberResponse;
import com.stoury.dto.feed.FeedCreateRequest;
import com.stoury.dto.feed.FeedResponse;
import com.stoury.dto.feed.FeedUpdateRequest;
import com.stoury.dto.feed.GraphicContentResponse;
import com.stoury.dto.feed.LocationResponse;
import com.stoury.event.FeedResponseCreateEvent;
import com.stoury.event.FeedResponseDeleteEvent;
import com.stoury.event.FeedResponseUpdateEvent;
import com.stoury.event.GraphicDeleteEvent;
import com.stoury.exception.authentication.NotAuthorizedException;
import com.stoury.exception.feed.FeedCreateException;
import com.stoury.exception.feed.FeedSearchException;
import com.stoury.exception.feed.FeedUpdateException;
import com.stoury.exception.member.MemberSearchException;
import com.stoury.projection.FeedResponseEntity;
import com.stoury.repository.ClickLogRepository;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.LikeRepository;
import com.stoury.repository.MemberRepository;
import com.stoury.service.location.LocationService;
import com.stoury.utils.JsonMapper;
import com.stoury.utils.PageSize;
import com.stoury.utils.SupportedFileType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.stoury.utils.Values.FEED_ID_NOT_NULL_MESSAGE;
import static com.stoury.utils.Values.MEMBER_ID_NOT_NULL_MESSAGE;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final GraphicContentService graphicContentService;
    private final FeedRepository feedRepository;
    private final MemberRepository memberRepository;
    private final LikeRepository likeRepository;
    private final TagService tagService;
    private final LocationService locationService;
    private final ApplicationEventPublisher eventPublisher;
    private final JsonMapper jsonMapper;
    private final ClickLogRepository clickLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FeedResponse createFeed(Long writerId, FeedCreateRequest feedCreateRequest,
                                   List<MultipartFile> graphicContentsFiles) {
        validate(writerId, feedCreateRequest, graphicContentsFiles);

        Member writer = memberRepository.findById(writerId).orElseThrow(MemberSearchException::new);
        List<GraphicContent> graphicContents = graphicContentService.createGraphicContents(graphicContentsFiles);
        LocationResponse locationResponse = locationService.getLocation(feedCreateRequest.latitude(), feedCreateRequest.longitude());

        Feed uploadedFeed = saveFeed(feedCreateRequest, writer, graphicContents, locationResponse);

        eventPublisher.publishEvent(new FeedResponseCreateEvent(this, uploadedFeed));

        return FeedResponse.from(uploadedFeed, 0);
    }

    private Feed saveFeed(FeedCreateRequest feedCreateRequest, Member writer,
                          List<GraphicContent> graphicContents, LocationResponse locationResponse) {
        Feed feedEntity = createFeedEntity(writer, feedCreateRequest, graphicContents, locationResponse);

        return feedRepository.save(feedEntity);
    }

    private Feed createFeedEntity(Member writer, FeedCreateRequest feedCreateRequest,
                                  List<GraphicContent> graphicContents, LocationResponse locationResponse) {
        Set<Tag> tags = getOrCreateTags(feedCreateRequest.tagNames());
        return feedCreateRequest.toEntity(writer, graphicContents, tags, locationResponse.city(), locationResponse.country());
    }

    private Set<Tag> getOrCreateTags(Set<String> tagNames) {
        return tagNames.stream()
                .map(tagService::getTagOrElseCreate)
                .collect(Collectors.toSet());
    }

    private void validate(Long writerId, FeedCreateRequest feedCreateRequest, List<MultipartFile> graphicContents) {
        validateId(writerId, () -> new FeedCreateException("Writer id cannot be null"));
        validateGraphicContents(graphicContents);
        validateFeedCreateRequest(feedCreateRequest);
    }

    private void validateFeedCreateRequest(FeedCreateRequest feedCreateRequest) {
        if (feedCreateRequest.longitude() == null || feedCreateRequest.latitude() == null) {
            throw new FeedCreateException("Location information is required.");
        }
    }

    private void validateId(Long id, Supplier< ? extends RuntimeException> exceptionSupplier) {
        if (id == null) {
            throw exceptionSupplier.get();
        }
    }

    private void validateGraphicContents(List<MultipartFile> graphicContents) {
        if (graphicContents.isEmpty() || graphicContents.stream().anyMatch(SupportedFileType::isUnsupportedFile)) {
            throw new FeedCreateException("Input files are empty or unsupported.");
        }
    }

    @Transactional(readOnly = true)
    public List<FeedResponse> getFeedsOfMemberId(Long memberId, Long offsetId) {
        validateId(memberId, () -> new FeedSearchException(MEMBER_ID_NOT_NULL_MESSAGE));
        validateId(offsetId, () -> new FeedSearchException("Offset id cannot be null."));

        Member feedWriter = memberRepository.findById(memberId)
                .orElseThrow(() -> new FeedCreateException("Cannot find the member."));

        Pageable page = PageRequest.of(0, PageSize.FEED_PAGE_SIZE, Sort.by("createdAt").descending());

        List<FeedResponseEntity> feeds = feedRepository.findAllFeedsByMemberAndIdLessThan(feedWriter, offsetId, page);

        return feeds.stream()
                .map(this::toFeedResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FeedResponse> getFeedsByTag(String tagName, Long offsetId) {
        validateId(offsetId, () -> new FeedSearchException("Offset id cannot be null."));

        Pageable page = PageRequest.of(0, PageSize.FEED_PAGE_SIZE, Sort.by("createdAt").descending());
        Long offsetIdNotNull = Objects.requireNonNull(offsetId);

        List<FeedResponseEntity> feeds = feedRepository.findByTagNameAndIdLessThan(tagName, offsetIdNotNull, page);

        return feeds.stream()
                .map(this::toFeedResponse)
                .toList();
    }

    private FeedResponse toFeedResponse(FeedResponseEntity feedResponseEntity) {
        String feedIdStr = feedResponseEntity.getFeedId().toString();

        SimpleMemberResponse writer = new SimpleMemberResponse(feedResponseEntity.getWriterId(), feedResponseEntity.getWriterUsername());
        List<GraphicContentResponse> graphicContents = jsonStringToGraphicContentResponseList(feedResponseEntity.getGraphicContentPaths());
        Set<String> tags = jsonStringToTagSet(feedResponseEntity.getTagNames());
        LocationResponse location = new LocationResponse(feedResponseEntity.getCity(), feedResponseEntity.getCountry());
        long likes = likeRepository.getCountByFeedId(feedIdStr);

        return FeedResponse.from(feedResponseEntity, writer, graphicContents, tags, location, likes);
    }

    private Set<String> jsonStringToTagSet(String tagNames) {
        return jsonMapper.stringJsonToObject(tagNames, new TypeReference<Set<String>>() {});
    }

    private List<GraphicContentResponse> jsonStringToGraphicContentResponseList(String graphicContentPaths) {
        return jsonMapper.stringJsonToObject(graphicContentPaths, new TypeReference<List<GraphicContentResponse>>() {});
    }

    protected FeedResponse updateFeed(Long feedId, FeedUpdateRequest feedUpdateRequest) {
        Feed feed = feedRepository.findById(Objects.requireNonNull(feedId))
                .orElseThrow(FeedSearchException::new);

        List<GraphicContent> beforeDeleteGraphicContents = new ArrayList<>(feed.getGraphicContents());

        feed.update(feedUpdateRequest);
        feed.updateTags(getOrCreateTags(feedUpdateRequest.tagNames()));
        feed.deleteSelectedGraphics(feedUpdateRequest.deleteGraphicContentSequence());

        List<GraphicContent> toDeleteGraphicContents = beforeDeleteGraphicContents.stream()
                .filter(graphicContent -> !feed.getGraphicContents().contains(graphicContent))
                .toList();

        publishDeleteFileEvents(toDeleteGraphicContents);
        eventPublisher.publishEvent(new FeedResponseUpdateEvent(this, feed.getId()));

        return FeedResponse.from(feed, likeRepository.getCountByFeedId(feed.getId().toString()));
    }

    @Transactional
    public FeedResponse updateFeedIfOwner(Long feedId, FeedUpdateRequest feedUpdateRequest, Long memberId) {
        validateId(feedId, () -> new FeedUpdateException(FEED_ID_NOT_NULL_MESSAGE));
        validateId(memberId, () -> new FeedUpdateException(MEMBER_ID_NOT_NULL_MESSAGE));

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(FeedSearchException::new);

        if (feed.notOwnedBy(memberId)) {
            throw new NotAuthorizedException();
        }

        return updateFeed(feedId, feedUpdateRequest);
    }

    private void publishDeleteFileEvents(List<GraphicContent> toDeleteGraphicContents) {
        for (GraphicContent toDeleteGraphicContent : toDeleteGraphicContents) {
            String path = toDeleteGraphicContent.getPath();
            eventPublisher.publishEvent(new GraphicDeleteEvent(this, path));
        }
    }

    protected void deleteFeed(Long feedId) {
        validateId(feedId);

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(FeedSearchException::new);

        feedRepository.delete(feed);

        publishDeleteFileEvents(feed.getGraphicContents());
        eventPublisher.publishEvent(new FeedResponseDeleteEvent(this, feedId));
    }

    @Transactional
    public void deleteFeedIfOwner(Long feedId, Long memberId) {
        validateId(feedId);

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(FeedSearchException::new);

        if (feed.notOwnedBy(memberId)) {
            throw new NotAuthorizedException();
        }

        deleteFeed(feedId);
    }

    @Transactional(readOnly = true)
    public FeedResponse getFeed(Long feedId) {
        validateId(feedId);

        FeedResponseEntity feedResponseEntity = feedRepository.findFeedResponseById(feedId)
                .orElseThrow(FeedSearchException::new);

        return toFeedResponse(feedResponseEntity);
    }

    @Transactional(readOnly = true)
    public List<FeedResponse> getRecommendedFeeds(Long memberId) {
        validateId(memberId);

        if (memberRepository.notExistsById(memberId)) {
            throw new MemberSearchException();
        }

        List<Tag> frequentTags = tagService.getFrequentTags(memberId);
        List<Long> recommendFeedIds = feedRepository.findRandomFeedIdsByTagName(frequentTags);
        List<FeedResponseEntity> recommendFeeds = feedRepository.findAllFeedsByIdIn(recommendFeedIds);

        return recommendFeeds.stream().map(this::toFeedResponse).toList();
    }

    private void validateId(Long id) {
        validateId(id, () -> new IllegalArgumentException("Id cannot be null."));
    }

    @Transactional(readOnly = true)
    public void clickLogUpdate(Long memberId, Long feedId) {
        validateId(memberId, () -> new IllegalArgumentException(MEMBER_ID_NOT_NULL_MESSAGE));
        validateId(feedId, () -> new IllegalArgumentException(FEED_ID_NOT_NULL_MESSAGE));

        if (memberRepository.notExistsById(memberId)) {
            throw new MemberSearchException();
        }
        if (feedRepository.notExistsById(feedId)) {
            throw new FeedSearchException();
        }

        ClickLog clickLog = ClickLog.builder()
                .memberId(memberId)
                .feedId(feedId)
                .createdAt(LocalDateTime.now())
                .build();

        clickLogRepository.save(clickLog);
    }

    @Transactional(readOnly = true)
    public List<FeedResponse> getFollowerViewedRecommendFeeds(Long memberId) {
        validateId(memberId, () -> new IllegalArgumentException(MEMBER_ID_NOT_NULL_MESSAGE));

        List<Long> recommendFeedIds = feedRepository.findRandomRecommendFeedIds(memberId);
        List<FeedResponseEntity> recommendFeeds = feedRepository.findAllFeedsByIdIn(recommendFeedIds);

        return recommendFeeds.stream()
                .map(this::toFeedResponse)
                .toList();
    }
}
