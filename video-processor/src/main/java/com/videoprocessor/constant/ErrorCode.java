package com.videoprocessor.constant;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INTERNAL_ERR(500, 1000, "Internal Error"),
    VALIDATION_ERROR(400, 1001, "Validation Error"),
    METHOD_NOT_ALLOWED(405, 1002, "Method Not Allowed"),
    NOT_FOUND(404, 1003, "Not Found"),
    INVALID_PATH(500,2000, "Invalid Path"),
    FILE_NOT_FOUND(500,2001, "File Not Found"),
    TRANSACTION_NOT_FOUND(404,3000, "Transaction Not Found"),;

    private final Integer status;
    private final Integer code;
    private final String message;

    ErrorCode(Integer status,Integer code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
