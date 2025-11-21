package br.com.eightbitbazar.domain.listing;

import br.com.eightbitbazar.domain.user.UserId;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public record Listing(
    ListingId id,
    UserId sellerId,
    String name,
    String description,
    Long platformId,
    Long manufacturerId,
    ItemCondition condition,
    int quantity,
    ListingType type,
    BigDecimal price,
    BigDecimal startingPrice,
    BigDecimal buyNowPrice,
    LocalDateTime auctionEndDate,
    BigDecimal cashDiscountPercent,
    ListingStatus status,
    List<ListingImage> images,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime deletedAt
) {
    public Listing {
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (platformId == null) {
            throw new IllegalArgumentException("Platform is required");
        }
        if (manufacturerId == null) {
            throw new IllegalArgumentException("Manufacturer is required");
        }
        if (condition == null) {
            throw new IllegalArgumentException("Condition is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type is required");
        }
        if (quantity < 0 || (quantity == 0 && status != ListingStatus.SOLD)) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (status == null) {
            status = ListingStatus.ACTIVE;
        }
        if (images == null) {
            images = List.of();
        }
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isAuction() {
        return type == ListingType.AUCTION;
    }

    public boolean isDirectSale() {
        return type == ListingType.DIRECT_SALE;
    }

    public boolean isShowcase() {
        return type == ListingType.SHOWCASE;
    }

    public boolean isActive() {
        return status == ListingStatus.ACTIVE;
    }

    public boolean hasAuctionEnded() {
        return isAuction() && auctionEndDate != null && LocalDateTime.now().isAfter(auctionEndDate);
    }

    public boolean hasCashDiscount() {
        return cashDiscountPercent != null && cashDiscountPercent.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal calculateCashPrice() {
        if (!hasCashDiscount() || price == null) {
            return price;
        }
        BigDecimal discount = price.multiply(cashDiscountPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.FLOOR);
        return price.subtract(discount);
    }

    public Listing withId(ListingId id) {
        return new Listing(id, sellerId, name, description, platformId, manufacturerId, condition, quantity,
            type, price, startingPrice, buyNowPrice, auctionEndDate, cashDiscountPercent, status, images,
            createdAt, updatedAt, deletedAt);
    }

    public Listing withStatus(ListingStatus status) {
        return new Listing(id, sellerId, name, description, platformId, manufacturerId, condition, quantity,
            type, price, startingPrice, buyNowPrice, auctionEndDate, cashDiscountPercent, status, images,
            createdAt, updatedAt, deletedAt);
    }

    public Listing withQuantity(int quantity) {
        return new Listing(id, sellerId, name, description, platformId, manufacturerId, condition, quantity,
            type, price, startingPrice, buyNowPrice, auctionEndDate, cashDiscountPercent, status, images,
            createdAt, updatedAt, deletedAt);
    }

    public Listing withDeletedAt(LocalDateTime deletedAt) {
        return new Listing(id, sellerId, name, description, platformId, manufacturerId, condition, quantity,
            type, price, startingPrice, buyNowPrice, auctionEndDate, cashDiscountPercent, status, images,
            createdAt, updatedAt, deletedAt);
    }
}
