package com.example.backend.dto;

public abstract class PcrResponse {
    protected String StatusMessage;
    protected String Guid;
    public abstract PcrResponseType getResponseType();
}
