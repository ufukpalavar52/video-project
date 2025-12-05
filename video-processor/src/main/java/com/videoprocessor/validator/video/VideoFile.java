package com.videoprocessor.validator.video;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = VideoFileValidator.class)
public @interface VideoFile {
    String message() default "Invalid video file.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] allowedTypes() default {
            "video/mp4",
            "video/mpeg",
            "video/ogg",
            "video/webm",
            "video/x-msvideo",
            "video/quicktime"
    };
}
