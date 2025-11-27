package br.com.eightbitbazar.adapter.in.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PaymentMethodValidator.class)
@Documented
public @interface ValidPaymentMethod {
    String message() default "Invalid payment method";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
