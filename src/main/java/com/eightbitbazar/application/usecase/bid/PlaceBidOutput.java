package com.eightbitbazar.application.usecase.bid;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlaceBidOutput(
    Long id,
    Long listingId,
    BigDecimal amount,
    LocalDateTime createdAt
) {}
