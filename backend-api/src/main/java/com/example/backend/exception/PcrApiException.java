package com.example.backend.exception;

public class PcrApiException extends RuntimeException{
    public PcrApiException(String message) {
        super(message);
    }

    public PcrApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
