package com.stoury.event;

import com.stoury.domain.Feed;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FeedResponseCreateEvent extends ApplicationEvent {
    private final Feed feed;
    public FeedResponseCreateEvent(Object source, Feed feed) {
        super(source);
        this.feed = feed;
    }
}
