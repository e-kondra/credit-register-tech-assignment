package com.example.backend.dto;

public class PcrErrorResponse extends PcrResponse{
    @Override
    public PcrResponseType getResponseType() {
        return PcrResponseType.ERROR;
    }
}
