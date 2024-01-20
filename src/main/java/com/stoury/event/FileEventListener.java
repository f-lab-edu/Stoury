package com.stoury.event;

import com.stoury.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class FileEventListener {
    private final FileService fileService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFileSaveEventHandler(FileSaveEvent fileSaveEvent) {
        Pair<MultipartFile, String> toSaveFiles = fileSaveEvent.getToSaveFile();
        fileService.saveFilesAtPath(toSaveFiles);
    }
}
