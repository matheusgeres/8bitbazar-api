package br.com.eightbitbazar.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PurchaseCompletedEvent(
    Long purchaseId,
    Long listingId,
    Long buyerId,
    Long sellerId,
    BigDecimal totalAmount,
    String paymentMethod,
    Instant occurredAt
) implements DomainEvent {

    public PurchaseCompletedEvent(Long purchaseId, Long listingId, Long buyerId, Long sellerId, BigDecimal totalAmount, String paymentMethod) {
        this(purchaseId, listingId, buyerId, sellerId, totalAmount, paymentMethod, Instant.now());
    }

    @Override
    public String eventType() {
        return "purchase.completed";
    }
}
