package com.example.backend.exception;

import org.springframework.http.HttpStatus;

public class CreditExtractNotFoundException extends ApiException{

    public CreditExtractNotFoundException(Long id) {
        super("Credit extract not found with ID: " + id,
                HttpStatus.NOT_FOUND,
                ErrorCode.CREDIT_EXTRACT_NOT_FOUND);
    }
}
