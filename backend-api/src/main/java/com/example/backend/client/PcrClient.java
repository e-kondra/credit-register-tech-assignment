package com.example.backend.client;

import com.example.backend.dto.PcrRequest;
import com.example.backend.dto.PcrResponse;
import com.example.backend.exception.PcrApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
@Slf4j
public class PcrClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${pcr.api.url}")
    private String apiUrl;

    @Value("${pcr.owner.idCodeType}")
    private String ownerIdCodeType;

    @Value("${pcr.owner.idCode}")
    private String ownerIdCode;

    @Value("${pcr.owner.countryCode}")
    private String ownerCountryCode;

    @Value("${pcr.targetEnvironment}")
    private String targetEnvironment;

    public PcrClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    public PcrResponse fetchCreditData(String ssn) {
        PcrRequest request = PcrRequest.builder()
                .targetEnvironment(targetEnvironment)
                .owner(PcrRequest.OwnerInfo.builder()
                        .idCodeType(ownerIdCodeType)
                        .idCode(ownerIdCode)
                        .countryCode(ownerCountryCode)
                        .build())
                .request(PcrRequest.RequestData.builder()
                        .idCodeType("PersonalIdentityCode")
                        .idCode(ssn)
                        .creditRegisterExtractPurpose(List.of("NewLoan"))
                        .build())
                .build();

        log.info("Calling PCR API for SSN: {}", ssn);

        try {
            PcrResponse response = restClient.post()
                    .uri(apiUrl)
                    .body(request)
                    .retrieve()
                    .body(PcrResponse.class);

            // Проверяем на ошибки
            if (response != null && response.getErrorResponses() != null && !response.getErrorResponses().isEmpty()) {
                String errorMsg = response.getErrorResponses().stream()
                        .map(e -> e.getErrorDescription())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("Unknown validation error");
                throw new PcrApiException("PCR API validation error: " + errorMsg);
            }

            return response;
        } catch (RestClientException e) {
            log.error("Failed to call PCR API: {}", e.getMessage());
            throw new PcrApiException("Failed to call PCR API: " + e.getMessage(), e);
        }
    }

}
