package com.example.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class PcrRequest {
    private String targetEnvironment;
    private OwnerInfo owner;
    private RequestData request;

    @Builder
    @Getter
    public static class OwnerInfo {
        private String idCodeType;
        private String idCode;
        private String countryCode;
    }

    @Builder
    @Getter
    public static class RequestData {
        private String idCodeType;
        private String idCode;
        private List<String> creditRegisterExtractPurpose;
    }

}
