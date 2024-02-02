package com.stoury.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class GraphicSaveEvent extends ApplicationEvent {
    private transient MultipartFile fileToSave;
    private String path;

    public GraphicSaveEvent(Object source, MultipartFile fileToSave, String path) {
        super(source);
        this.fileToSave = fileToSave;
        this.path = path;
    }
}
