package br.com.eightbitbazar.adapter.in.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ItemConditionValidator implements ConstraintValidator<ValidItemCondition, String> {

    private static final String[] VALID_CONDITIONS = {"SEALED", "COMPLETE", "LOOSE", "DAMAGED"};

    @Override
    public void initialize(ValidItemCondition constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String upperValue = value.toUpperCase();
        for (String condition : VALID_CONDITIONS) {
            if (condition.equals(upperValue)) {
                return true;
            }
        }
        return false;
    }
}

