package com.stoury.event;

import org.springframework.context.ApplicationEvent;

public class UpdatePopularAbroadSpots extends ApplicationEvent {
    public UpdatePopularAbroadSpots(Object source) {
        super(source);
    }
}
