package com.eightbitbazar.application.port.out;

import com.eightbitbazar.domain.event.DomainEvent;

public interface EventPublisher {
    void publish(DomainEvent event);
}
