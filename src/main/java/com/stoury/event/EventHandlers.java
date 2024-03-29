package com.stoury.event;

import com.stoury.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class EventHandlers {
    private final StorageService storageService;

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
}
