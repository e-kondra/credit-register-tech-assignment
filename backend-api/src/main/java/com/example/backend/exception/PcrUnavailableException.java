package com.example.backend.exception;

import org.springframework.http.HttpStatus;

public class PcrUnavailableException extends ApiException{

    public PcrUnavailableException(String message){
        super(message, HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.PCR_UNAVAILABLE);
    }

    public PcrUnavailableException(String message, Throwable cause){
        super(message, cause, HttpStatus.SERVICE_UNAVAILABLE,  ErrorCode.PCR_UNAVAILABLE);
    }
}
