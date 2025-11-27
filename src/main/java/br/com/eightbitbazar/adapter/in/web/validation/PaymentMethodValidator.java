package br.com.eightbitbazar.adapter.in.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PaymentMethodValidator implements ConstraintValidator<ValidPaymentMethod, String> {

    private static final String[] VALID_PAYMENT_METHODS = {"PIX", "CASH", "CREDIT_CARD", "DEBIT_CARD", "OTHER"};

    @Override
    public void initialize(ValidPaymentMethod constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String upperValue = value.toUpperCase();
        for (String paymentMethod : VALID_PAYMENT_METHODS) {
            if (paymentMethod.equals(upperValue)) {
                return true;
            }
        }
        return false;
    }
}
