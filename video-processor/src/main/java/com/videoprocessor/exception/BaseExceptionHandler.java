package com.videoprocessor.exception;

import com.videoprocessor.constant.ErrorCode;
import com.videoprocessor.model.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;


@Slf4j
@RestControllerAdvice
public class BaseExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknownException(Exception ex, HttpServletRequest req) {
        log.error("[UnknownException={0}]", ex);
        return errorResponse(ErrorCode.INTERNAL_ERR);
    }

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<ErrorResponse> handleCommonException(CommonException ex, HttpServletRequest req) {
        log.error("[CommonException={0}]", ex);
        return errorResponse(ex.getStatus(), ex.getErrorCode().getCode(), ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NoResourceFoundException ex, HttpServletRequest req) {
        log.error("[NoResourceFoundException={0}]", ex);
        ErrorCode errorCode = ErrorCode.NOT_FOUND;
        return errorResponse(errorCode.getStatus(), errorCode.getCode(), ex.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, HttpServletRequest req) {
        log.error("[ValidationException={0}]", ex);
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        return errorResponse(errorCode.getStatus(), errorCode.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest req) {
        log.error("[MethodArgumentNotValidException={0}]", ex);
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        List<ObjectError> objectErrors = ex.getBindingResult().getAllErrors();
        int lastIndex = objectErrors.size() - 1;
        String message = objectErrors.get(lastIndex).getDefaultMessage();

        return errorResponse(errorCode.getStatus(), errorCode.getCode(), message);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        log.error("[HttpRequestMethodNotSupportedException={0}]", ex);
        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
        return errorResponse(errorCode.getStatus(), errorCode.getCode(), ex.getMessage());
    }

    public ResponseEntity<ErrorResponse> errorResponse(ErrorCode errorCode) {
        return errorResponse(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage());
    }

    public ResponseEntity<ErrorResponse> errorResponse(Integer status, Integer code, String message) {
        ErrorResponse response = ErrorResponse
                .builder()
                .message(message)
                .code(code)
                .build();
        return ResponseEntity.status(status).body(response);
    }
}
