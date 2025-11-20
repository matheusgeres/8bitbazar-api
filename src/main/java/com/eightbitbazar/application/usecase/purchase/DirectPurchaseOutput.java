package com.eightbitbazar.application.usecase.purchase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DirectPurchaseOutput(
    Long id,
    Long listingId,
    BigDecimal amount,
    BigDecimal discountApplied,
    BigDecimal finalAmount,
    String paymentMethod,
    String status,
    LocalDateTime createdAt
) {}
