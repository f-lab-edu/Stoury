package com.stoury.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class FileSaveEvent extends ApplicationEvent {
    private Pair<MultipartFile, String> toSaveFile;

    public FileSaveEvent(Object source, Pair<MultipartFile, String> toSaveFiles) {
        super(source);
        this.toSaveFile = toSaveFiles;
    }
}
