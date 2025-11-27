package br.com.eightbitbazar.adapter.in.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ListingTypeValidator implements ConstraintValidator<ValidListingType, String> {

    private static final String[] VALID_TYPES = {"AUCTION", "DIRECT_SALE", "SHOWCASE"};

    @Override
    public void initialize(ValidListingType constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String upperValue = value.toUpperCase();
        for (String type : VALID_TYPES) {
            if (type.equals(upperValue)) {
                return true;
            }
        }
        return false;
    }
}

