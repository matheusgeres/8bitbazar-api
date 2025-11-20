package com.eightbitbazar.application.usecase.bid;

import java.math.BigDecimal;

public record PlaceBidInput(
    Long listingId,
    BigDecimal amount
) {}
