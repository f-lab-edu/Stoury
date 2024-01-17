package com.stoury.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Getter
public class FileSaveEvent extends ApplicationEvent {
    private Map<MultipartFile, String> toSaveFiles;

    public FileSaveEvent(Object source, Map<MultipartFile, String> toSaveFiles) {
        super(source);
        this.toSaveFiles = toSaveFiles;
    }
}
