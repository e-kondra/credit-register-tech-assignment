package com.example.backend.exception;

import org.springframework.http.HttpStatus;

public class PcrValidationException extends ApiException{

    public PcrValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ErrorCode.PCR_VALIDATION_ERROR);
    }

    public PcrValidationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, ErrorCode.PCR_VALIDATION_ERROR);
    }
}
