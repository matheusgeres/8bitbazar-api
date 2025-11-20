package com.eightbitbazar.application.usecase.listing;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateListingInput(
    String name,
    String description,
    Long platformId,
    Long manufacturerId,
    String condition,
    int quantity,
    String type,
    BigDecimal price,
    BigDecimal startingPrice,
    BigDecimal buyNowPrice,
    LocalDateTime auctionEndDate,
    BigDecimal cashDiscountPercent
) {}
