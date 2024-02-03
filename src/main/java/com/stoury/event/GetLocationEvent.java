package com.stoury.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class GetLocationEvent extends ApplicationEvent {
    private Long feedId;
    private Double latitude;
    private Double longitude;
    public GetLocationEvent(Object source, Long feedId, Double latitude, Double longitude) {
        super(source);
        this.feedId = feedId;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
