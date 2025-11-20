package com.eightbitbazar.domain.listing;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceHistory(
    Long id,
    ListingId listingId,
    BigDecimal oldPrice,
    BigDecimal newPrice,
    LocalDateTime createdAt
) {
    public PriceHistory {
        if (listingId == null) {
            throw new IllegalArgumentException("Listing is required");
        }
        if (oldPrice == null) {
            throw new IllegalArgumentException("Old price is required");
        }
        if (newPrice == null) {
            throw new IllegalArgumentException("New price is required");
        }
    }

    public PriceHistory withId(Long id) {
        return new PriceHistory(id, listingId, oldPrice, newPrice, createdAt);
    }

    public BigDecimal getDiscountAmount() {
        return oldPrice.subtract(newPrice);
    }

    public BigDecimal getDiscountPercent() {
        if (oldPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getDiscountAmount()
            .multiply(BigDecimal.valueOf(100))
            .divide(oldPrice, 2, java.math.RoundingMode.HALF_UP);
    }
}
