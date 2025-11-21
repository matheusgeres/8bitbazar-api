package br.com.eightbitbazar.domain.purchase;

import br.com.eightbitbazar.domain.listing.ListingId;
import br.com.eightbitbazar.domain.user.UserId;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Purchase(
    Long id,
    ListingId listingId,
    UserId buyerId,
    UserId sellerId,
    BigDecimal amount,
    PurchaseType type,
    PaymentMethod paymentMethod,
    BigDecimal discountApplied,
    BigDecimal finalAmount,
    PurchaseStatus status,
    LocalDateTime createdAt
) {
    public Purchase {
        if (listingId == null) {
            throw new IllegalArgumentException("Listing is required");
        }
        if (buyerId == null) {
            throw new IllegalArgumentException("Buyer is required");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller is required");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (type == null) {
            throw new IllegalArgumentException("Purchase type is required");
        }
        if (finalAmount == null || finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Final amount must be positive");
        }
        if (status == null) {
            status = PurchaseStatus.PENDING;
        }
        if (discountApplied == null) {
            discountApplied = BigDecimal.ZERO;
        }
    }

    public Purchase withId(Long id) {
        return new Purchase(id, listingId, buyerId, sellerId, amount, type, paymentMethod, discountApplied, finalAmount, status, createdAt);
    }

    public Purchase withStatus(PurchaseStatus status) {
        return new Purchase(id, listingId, buyerId, sellerId, amount, type, paymentMethod, discountApplied, finalAmount, status, createdAt);
    }
}
