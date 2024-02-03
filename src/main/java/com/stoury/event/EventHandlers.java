package com.stoury.event;

import com.stoury.repository.FeedRepository;
import com.stoury.repository.RankingRepository;
import com.stoury.service.storage.StorageService;
import com.stoury.utils.CacheKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EventHandlers {
    private final StorageService storageService;
    private final FeedRepository feedRepository;
    private final RankingRepository rankingRepository;

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

    @EventListener
    public void onUpdatePopularDomesticSpotsHandler(UpdatePopularDomesticSpots event) {
        Pageable pageable = PageRequest.of(0, 10);
        List<String> rankedCities = feedRepository.findTop10CitiesInKorea(pageable);
        rankingRepository.update(CacheKeys.POPULAR_DOMESTIC_SPOTS, rankedCities);
    }

    @EventListener
    public void onUpdatePopularAbroadSpotsHandler(UpdatePopularAbroadSpots event) {
        Pageable pageable = PageRequest.of(0, 10);
        List<String> rankedCities = feedRepository.findTop10CountriesNotKorea(pageable);
        rankingRepository.update(CacheKeys.POPULAR_ABROAD_SPOTS, rankedCities);
    }
}
