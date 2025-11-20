package com.eightbitbazar.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

public record BidPlacedEvent(
    Long bidId,
    Long listingId,
    Long bidderId,
    BigDecimal amount,
    Instant occurredAt
) implements DomainEvent {

    public BidPlacedEvent(Long bidId, Long listingId, Long bidderId, BigDecimal amount) {
        this(bidId, listingId, bidderId, amount, Instant.now());
    }

    @Override
    public String eventType() {
        return "bid.placed";
    }
}
