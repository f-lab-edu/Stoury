package com.stoury.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class GraphicSaveEvent extends ApplicationEvent {
    private Pair<MultipartFile, String> toSaveFile;

    public GraphicSaveEvent(Object source, Pair<MultipartFile, String> toSaveFiles) {
        super(source);
        this.toSaveFile = toSaveFiles;
    }
}
