package br.com.eightbitbazar.adapter.in.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ListingTypeValidator.class)
@Documented
public @interface ValidListingType {
    String message() default "Invalid listing type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

