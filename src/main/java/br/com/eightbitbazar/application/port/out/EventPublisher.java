package br.com.eightbitbazar.application.port.out;

import br.com.eightbitbazar.domain.event.DomainEvent;

public interface EventPublisher {
    void publish(DomainEvent event);
}
