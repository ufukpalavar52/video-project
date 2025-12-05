package com.videoprocessor.exception;

import com.videoprocessor.constant.ErrorCode;
import lombok.Getter;

@Getter
public class CommonException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Integer status;

    public CommonException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.status = errorCode.getStatus();
    }

    public CommonException(ErrorCode errorCode, Integer status) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.status = status;
    }

    public CommonException(ErrorCode errorCode, Integer status, String message) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
}
