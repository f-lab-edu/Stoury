package com.stoury.event;

import com.stoury.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class FileEventListener {
    private final FileService fileService;

    @TransactionalEventListener
    public void onFileSaveEventHandler(FileSaveEvent fileSaveEvent) {
        Map<MultipartFile, String> toSaveFiles = fileSaveEvent.getToSaveFiles();
        fileService.saveFilesAtPaths(toSaveFiles);
    }
}