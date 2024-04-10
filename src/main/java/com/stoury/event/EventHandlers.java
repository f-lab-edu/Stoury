package com.stoury.event;

import com.stoury.domain.Feed;
import com.stoury.domain.GraphicContent;
import com.stoury.domain.Member;
import com.stoury.domain.Tag;
import com.stoury.exception.feed.FeedSearchException;
import com.stoury.exception.graphiccontent.GraphicContentsException;
import com.stoury.projection.FeedResponseEntity;
import com.stoury.repository.FeedRepository;
import com.stoury.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventHandlers {
    private final StorageService storageService;
    private final FeedRepository feedRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void onFeedCreationFailEventHandler(GraphicSaveEvent graphicSaveEvent) {
        String path = graphicSaveEvent.getPath();
        storageService.deleteFileAtPath(Paths.get(path));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFileDeleteEventHandler(GraphicDeleteEvent graphicDeleteEvent) {
        String path = graphicDeleteEvent.getPath();
        try {
            storageService.deleteFileAtPath(Paths.get(path));
        } catch (GraphicContentsException exception) {
            log.error(exception.getMessage());
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFeedResponseCreateEventHandler(FeedResponseCreateEvent feedResponseCreateEvent) {
        Feed feed = feedResponseCreateEvent.getFeed();
        Member writer = feed.getMember();
        List<GraphicContent> graphicContents = feed.getGraphicContents();
        Set<Tag> tags = feed.getTags();

        Long writerId = writer.getId();
        String writerUsername = writer.getUsername();

        String graphicContentPaths = concat(graphicContents);
        String tagNames = concat(tags);

        FeedResponseEntity feedResponse = new FeedResponseEntity(
                feed.getId(),
                writerId, writerUsername, graphicContentPaths,
                tagNames, feed.getCreatedAt(), feed.getTextContent(),
                feed.getLatitude(), feed.getLongitude(), feed.getCity(),
                feed.getCountry());

        feedRepository.saveFeedResponse(feedResponse);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFeedResponseUpdateEventHandler(FeedResponseUpdateEvent feedResponseUpdateEvent) {
        Long feedId = feedResponseUpdateEvent.getFeedId();
        FeedResponseEntity feedResponse = feedRepository.findFeedResponseById(feedId).orElseThrow(FeedSearchException::new);
        Feed feedEntity = feedRepository.findById(feedId).orElseThrow(FeedSearchException::new);

        String graphicContentPaths = concat(feedEntity.getGraphicContents());
        String tagNames = concat(feedEntity.getTags());

        feedResponse.update(
                graphicContentPaths,
                tagNames,
                feedEntity.getCreatedAt(),
                feedEntity.getTextContent(),
                feedEntity.getLatitude(),
                feedEntity.getLongitude(),
                feedEntity.getCity(),
                feedEntity.getCountry());

    }

    private String concat(List<GraphicContent> graphicContents) {
        return graphicContents.stream()
                .map(this::getJsonRaw)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String concat(Set<Tag> tags) {
        return tags.stream()
                .map(Tag::getTagName)
                .collect(Collectors.joining("\", \"", "[\"", "\"]"));
    }

    private String getJsonRaw(GraphicContent graphicContent) {
        return "{\"id\":%d, \"path\":\"%s\"}".formatted(graphicContent.getId(), graphicContent.getPath());
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFeedResponseDeleteEventHandler(FeedResponseDeleteEvent feedResponseDeleteEvent) {
        Long feedId = feedResponseDeleteEvent.getFeedId();

        feedRepository.deleteFeedResponseById(feedId);
    }
}
