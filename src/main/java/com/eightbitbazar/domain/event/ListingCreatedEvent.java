package com.eightbitbazar.domain.event;

import java.time.Instant;

public record ListingCreatedEvent(
    Long listingId,
    Long sellerId,
    String listingName,
    String listingType,
    Instant occurredAt
) implements DomainEvent {

    public ListingCreatedEvent(Long listingId, Long sellerId, String listingName, String listingType) {
        this(listingId, sellerId, listingName, listingType, Instant.now());
    }

    @Override
    public String eventType() {
        return "listing.created";
    }
}
