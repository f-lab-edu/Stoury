package com.stoury.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FeedResponseDeleteEvent extends ApplicationEvent {
    private final Long feedId;
    public FeedResponseDeleteEvent(Object source, Long feedId) {
        super(source);
        this.feedId = feedId;
    }
}
