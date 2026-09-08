package com.example.backend.dto;

public class PcrSuccessResponse extends PcrResponse{

    @Override
    public PcrResponseType getResponseType() {
        return PcrResponseType.SUCCESS;
    }
}
