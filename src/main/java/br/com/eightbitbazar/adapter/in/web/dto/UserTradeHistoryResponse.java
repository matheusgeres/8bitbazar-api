package br.com.eightbitbazar.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserTradeHistoryResponse(
    Long purchaseId,
    Long listingId,
    String listingTitle,
    String listingType,
    String listingStatus,
    BigDecimal amount,
    BigDecimal finalAmount,
    String paymentMethod,
    String purchaseStatus,
    LocalDateTime createdAt
) {
}
