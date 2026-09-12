package com.example.backend.exception;

import org.springframework.http.HttpStatus;

public class InvalidSsnException extends ApiException{

    public InvalidSsnException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ErrorCode.INVALID_SSN);
    }
}
