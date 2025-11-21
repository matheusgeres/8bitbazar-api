package br.com.eightbitbazar.domain.event;

import java.time.Instant;

public record ListingDeletedEvent(
    Long listingId,
    Long sellerId,
    Instant occurredAt
) implements DomainEvent {

    public ListingDeletedEvent(Long listingId, Long sellerId) {
        this(listingId, sellerId, Instant.now());
    }

    @Override
    public String eventType() {
        return "listing.deleted";
    }
}
