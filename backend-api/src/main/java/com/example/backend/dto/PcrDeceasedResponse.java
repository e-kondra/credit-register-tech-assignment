package com.example.backend.dto;

public class PcrDeceasedResponse extends PcrResponse{
    @Override
    public PcrResponseType getResponseType() {
        return PcrResponseType.DECEASED;
    }
}
