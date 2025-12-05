package com.videoprocessor.validator.video;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class VideoFileValidator implements ConstraintValidator<VideoFile, MultipartFile> {
    private List<String> allowed;

    @Override
    public void initialize(VideoFile constraintAnnotation) {
        allowed = Arrays.asList(constraintAnnotation.allowedTypes());
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        return contentType != null && allowed.contains(contentType);
    }
}
