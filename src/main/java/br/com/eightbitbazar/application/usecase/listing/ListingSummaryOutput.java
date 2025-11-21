package br.com.eightbitbazar.application.usecase.listing;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ListingSummaryOutput(
    Long id,
    String name,
    String platform,
    String manufacturer,
    String condition,
    String type,
    BigDecimal price,
    BigDecimal startingPrice,
    LocalDateTime auctionEndDate,
    BigDecimal cashDiscountPercent,
    String status,
    List<String> imageUrls,
    String sellerNickname,
    LocalDateTime createdAt
) {}
