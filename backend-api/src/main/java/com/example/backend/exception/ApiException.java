package com.example.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final ErrorCode errorCode;

    protected ApiException(String message, HttpStatus status, ErrorCode errorCode) {
        super(message);
        this.httpStatus = status;
        this.errorCode = errorCode;
    }

    protected ApiException(String message, Throwable cause, HttpStatus status, ErrorCode errorCode) {
        super(message, cause);
        this.httpStatus = status;
        this.errorCode = errorCode;
    }

}
