package com.eightbitbazar.domain.bid;

import com.eightbitbazar.domain.listing.ListingId;
import com.eightbitbazar.domain.user.UserId;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Bid(
    Long id,
    ListingId listingId,
    UserId userId,
    BigDecimal amount,
    LocalDateTime createdAt
) {
    public Bid {
        if (listingId == null) {
            throw new IllegalArgumentException("Listing is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    public Bid withId(Long id) {
        return new Bid(id, listingId, userId, amount, createdAt);
    }
}
