package com.stoury.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class GraphicDeleteEvent extends ApplicationEvent {
    private String path;

    public GraphicDeleteEvent(Object source, String path) {
        super(source);
        this.path = path;
    }
}
