package com.videoprocessor.validator.greater;

import com.videoprocessor.validator.video.VideoFileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;


@Target({ElementType.TYPE}) // Class-level usage
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(IsGreaterThan.List.class)
@Constraint(validatedBy = IsGreaterThanValidator.class)
public @interface IsGreaterThan {

    String message() default "The starting value cannot be greater than the ending value."; // Default error message

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String field() default ""; // The starting field

    String greaterThanField() default "";

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        IsGreaterThan[] value();
    }
}
