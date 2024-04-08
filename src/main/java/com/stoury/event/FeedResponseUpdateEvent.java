package com.stoury.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FeedResponseUpdateEvent extends ApplicationEvent {
    private final Long feedId;
    public FeedResponseUpdateEvent(Object source, Long feedId) {
        super(source);
        this.feedId = feedId;
    }
}
