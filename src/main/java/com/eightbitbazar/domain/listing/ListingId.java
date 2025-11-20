package com.eightbitbazar.domain.listing;

public record ListingId(Long value) {

    public ListingId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ListingId must be a positive number");
        }
    }
}
