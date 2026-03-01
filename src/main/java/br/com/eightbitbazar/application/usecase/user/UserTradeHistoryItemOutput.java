package br.com.eightbitbazar.application.usecase.user;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserTradeHistoryItemOutput(
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
