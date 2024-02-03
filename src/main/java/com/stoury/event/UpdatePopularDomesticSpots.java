package com.stoury.event;

import org.springframework.context.ApplicationEvent;

public class UpdatePopularDomesticSpots extends ApplicationEvent {
    public UpdatePopularDomesticSpots(Object source) {
        super(source);
    }
}
