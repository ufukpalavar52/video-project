package com.videoprocessor.util;

import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    static class DummyRequest {
        @NotBlank(message = "Name cannot be blank")
        private String name;

        @NotNull(message = "Age is required")
        private Integer age;

        public DummyRequest(String name, Integer age) {
            this.name = name;
            this.age = age;
        }
    }

    @Test
    void validateRequest_WithValidRequest_ShouldNotThrowException() {
        DummyRequest validRequest = new DummyRequest("John", 25);

        assertDoesNotThrow(() -> ValidationUtils.validateRequest(validRequest, validator));
    }

    @Test
    void validateRequest_WithInvalidRequest_ShouldThrowValidationException() {
        DummyRequest invalidRequest = new DummyRequest("", null);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ValidationUtils.validateRequest(invalidRequest, validator)
        );

        String expectedMessagePart1 = "name: Name cannot be blank";
        String expectedMessagePart2 = "age: Age is required";

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains(expectedMessagePart1));
        assertTrue(exception.getMessage().contains(expectedMessagePart2));

        assertTrue(exception.getMessage().contains(", "));
    }
}