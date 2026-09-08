package com.example.backend.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Builder
@RequiredArgsConstructor
public class PcrRequest {
    private String targetEnvironment;
    private OwnerInfo owner;
    private RequestData request;


    @Builder
    @RequiredArgsConstructor
    public static class OwnerInfo {
        private String idCodeType;
        private String idCode;
        private String countryCode;
    }

    @Builder
    @RequiredArgsConstructor
    public static class RequestData {
        private String idCodeType;
        private String idCode;
        private List<String> creditRegisterExtractPurpose;
    }

}
