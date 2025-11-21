package br.com.eightbitbazar.application.usecase.listing;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ListingDetailsOutput(
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
    BigDecimal cashPrice,
    String status,
    List<String> imageUrls,
    SellerInfo seller,
    List<BidInfo> recentBids,
    LocalDateTime createdAt
) {
    public record SellerInfo(
        Long id,
        String nickname,
        String whatsappLink
    ) {}

    public record BidInfo(
        Long id,
        String bidderNickname,
        BigDecimal amount,
        LocalDateTime createdAt
    ) {}
}
