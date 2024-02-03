package com.stoury.event;

import com.stoury.domain.Feed;
import com.stoury.dto.LocationResponse;
import com.stoury.exception.feed.FeedSearchException;
import com.stoury.repository.FeedRepository;
import com.stoury.service.location.LocationService;
import com.stoury.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class EventHandlers {
    private final StorageService storageService;
    private final LocationService locationService;
    private final FeedRepository feedRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFileSaveEventHandler(GraphicSaveEvent graphicSaveEvent) {
        MultipartFile fileToSave = graphicSaveEvent.getFileToSave();
        String path = graphicSaveEvent.getPath();
        storageService.saveFileAtPath(fileToSave, Paths.get(path));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFileDeleteEventHandler(GraphicDeleteEvent graphicDeleteEvent) {
        String path = graphicDeleteEvent.getPath();
        storageService.deleteFileAtPath(Paths.get(path));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGetLocationEventHandler(GetLocationEvent getLocationEvent) {
        LocationResponse location = locationService.getLocationFrom(getLocationEvent.getLatitude(), getLocationEvent.getLongitude());

        Long feedId = Objects.requireNonNull(getLocationEvent.getFeedId());
        Feed feed = feedRepository.findById(feedId).orElseThrow(FeedSearchException::new);

        feed.updateLocation(location.city(), location.country());
    }
}
