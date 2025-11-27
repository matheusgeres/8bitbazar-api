package br.com.eightbitbazar.adapter.in.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ItemConditionValidator.class)
@Documented
public @interface ValidItemCondition {
    String message() default "Invalid item condition";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

