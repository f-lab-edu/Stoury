package com.stoury.event;

import com.stoury.domain.Feed;
import com.stoury.domain.GraphicContent;
import com.stoury.domain.Member;
import com.stoury.domain.Tag;
import com.stoury.projection.FeedResponseEntity;
import com.stoury.repository.FeedRepository;
import com.stoury.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventHandlers {
    private final StorageService storageService;
    private final FeedRepository feedRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFileSaveEventHandler(GraphicSaveEvent graphicSaveEvent) {
        MultipartFile fileToSave = graphicSaveEvent.getFileToSave();
        String path = graphicSaveEvent.getPath();
        storageService.saveFileAtPath(fileToSave, Paths.get(path));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFileDeleteEventHandler(GraphicDeleteEvent graphicDeleteEvent) {
        String path = graphicDeleteEvent.getPath();
        storageService.deleteFileAtPath(Paths.get(path));
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
}
