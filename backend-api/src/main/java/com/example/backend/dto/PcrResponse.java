package com.example.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class PcrResponse {
    private String statusMessage ;
    private String guid;
    private CreditRegisterExtract creditRegisterExtract;
    private List<ErrorResponse> errorResponses;
}
