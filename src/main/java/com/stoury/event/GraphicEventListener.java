package com.stoury.event;

import com.stoury.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class GraphicEventListener {
    private final StorageService storageService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFileSaveEventHandler(GraphicSaveEvent graphicSaveEvent) {
        MultipartFile fileToSave = graphicSaveEvent.getFileToSave();
        String path = graphicSaveEvent.getPath();
        storageService.saveFilesAtPath(fileToSave, path);
    }
}
