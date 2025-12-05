package com.videoprocessor.validator.greater;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class IsGreaterThanValidator implements ConstraintValidator<IsGreaterThan, Object> {

    private String field;
    private String greaterThanField;
    private String message;

    @Override
    public void initialize(IsGreaterThan constraintAnnotation) {
        this.field = constraintAnnotation.field();
        this.greaterThanField = constraintAnnotation.greaterThanField();
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            // Use Spring's BeanWrapperImpl to access field values dynamically
            Object fieldValue = new BeanWrapperImpl(value).getPropertyValue(field);
            Object greaterThanFieldValue = new BeanWrapperImpl(value).getPropertyValue(greaterThanField);

            // Check for null values
            if (fieldValue == null || greaterThanFieldValue == null) {
                return true; // Should be handled by another constraint like @NotNull
            }

            // Comparison Logic: 'fieldValue' must be less than or equal to 'greaterThanFieldValue'
            if (((Comparable) fieldValue).compareTo(greaterThanFieldValue) < 1) {
                return true; // Validation successful (field <= greaterThanField)
            }

            // If validation fails, disable the default message and add a custom violation
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(field) // Indicate which field the error is associated with
                    .addConstraintViolation();

            return false; // Validation failed

        } catch (final Exception e) {
            return false;
        }
    }
}