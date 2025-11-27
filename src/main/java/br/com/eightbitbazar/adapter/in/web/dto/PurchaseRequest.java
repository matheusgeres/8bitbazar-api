package br.com.eightbitbazar.adapter.in.web.dto;

import br.com.eightbitbazar.adapter.in.web.validation.ValidPaymentMethod;
import jakarta.validation.constraints.NotBlank;

public record PurchaseRequest(
        @NotBlank(message = "Payment method is required")
        @ValidPaymentMethod(message = "Invalid payment method. Valid values are: PIX, CASH, CREDIT_CARD, DEBIT_CARD, OTHER")
        String paymentMethod
) {}
