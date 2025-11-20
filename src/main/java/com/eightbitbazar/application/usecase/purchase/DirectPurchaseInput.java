package com.eightbitbazar.application.usecase.purchase;

public record DirectPurchaseInput(
    Long listingId,
    String paymentMethod
) {}
