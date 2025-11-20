package com.eightbitbazar.application.usecase.listing;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateListingOutput(
    Long id,
    String name,
    String description,
    String platform,
    String manufacturer,
    String condition,
    int quantity,
    String type,
    BigDecimal price,
    BigDecimal startingPrice,
    BigDecimal buyNowPrice,
    LocalDateTime auctionEndDate,
    BigDecimal cashDiscountPercent,
    String status,
    LocalDateTime createdAt
) {}
