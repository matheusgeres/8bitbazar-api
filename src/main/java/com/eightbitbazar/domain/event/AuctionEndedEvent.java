package com.eightbitbazar.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

public record AuctionEndedEvent(
    Long listingId,
    Long sellerId,
    Long winnerId,
    BigDecimal winningBid,
    Instant occurredAt
) implements DomainEvent {

    public AuctionEndedEvent(Long listingId, Long sellerId, Long winnerId, BigDecimal winningBid) {
        this(listingId, sellerId, winnerId, winningBid, Instant.now());
    }

    @Override
    public String eventType() {
        return "auction.ended";
    }
}
