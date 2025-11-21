package br.com.eightbitbazar.domain.event;

import java.time.Instant;

public record ListingSoldEvent(
    Long listingId,
    Long buyerId,
    Long sellerId,
    Instant occurredAt
) implements DomainEvent {

    public ListingSoldEvent(Long listingId, Long buyerId, Long sellerId) {
        this(listingId, buyerId, sellerId, Instant.now());
    }

    @Override
    public String eventType() {
        return "listing.sold";
    }
}
