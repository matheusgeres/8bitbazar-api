package com.eightbitbazar.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ListingResponse(
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
    List<String> imageUrls,
    LocalDateTime createdAt
) {}
