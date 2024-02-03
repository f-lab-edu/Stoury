package com.stoury.scheduler;

import com.stoury.event.UpdatePopularAbroadSpots;
import com.stoury.event.UpdatePopularDomesticSpots;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Scheduler {
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(fixedRate = 12 * 60 * 60 * 1000)
    public void updatePopularSpotsCache() {
        eventPublisher.publishEvent(new UpdatePopularDomesticSpots(this));
        eventPublisher.publishEvent(new UpdatePopularAbroadSpots(this));
    }
}
