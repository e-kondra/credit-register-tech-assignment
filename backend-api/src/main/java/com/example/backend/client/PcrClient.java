package com.example.backend.client;

import com.example.backend.dto.PcrRequest;
import com.example.backend.dto.PcrResponse;
import com.example.backend.exception.PcrApiException;
import com.example.backend.exception.PcrUnavailableException;
import com.example.backend.exception.PcrValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.stream.Collectors;

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

        PcrRequest request = buildRequest(ssn);

        log.info("Calling PCR API for SSN: {}", ssn);

        try {
            PcrResponse response = restClient.post()
                    .uri(apiUrl)
                    .body(request)
                    .retrieve()
                    .body(PcrResponse.class);

            if (response == null) {
                throw new PcrUnavailableException("PCR API returned empty response");
            }

            log.debug("PCR response received for SSN: {}", ssn);
            return response;

        } catch (RestClientResponseException exception) {
            int status = exception.getStatusCode().value();
            log.error("PCR returned HTTP {} for SSN {}: {}", status, ssn, exception.getResponseBodyAsString());

            if (status >= 400 && status < 500) {
                throw new PcrValidationException("PCR rejected request: " + exception.getStatusText(), exception);
            } else {
                throw new PcrUnavailableException("PCR server error: " + status, exception);
            }
        } catch (ResourceAccessException exception) {
            // Timeout or connection refused
            log.error("PCR API unreachable for SSN {}: {}", ssn, exception.getMessage());
            throw new PcrUnavailableException("PCR API is unreachable: " + exception.getMessage(), exception);
        } catch (RestClientException exception) {
            log.error("PCR client error for SSN {}: {}", ssn, exception.getMessage());
            throw new PcrUnavailableException("PCR API error: " + exception.getMessage(), exception);
        }
    }

    private PcrRequest buildRequest(String ssn) {
        return PcrRequest.builder()
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
    }


}
