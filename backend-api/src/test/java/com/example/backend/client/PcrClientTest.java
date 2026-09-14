package com.example.backend.client;

import com.example.backend.dto.PcrResponse;
import com.example.backend.exception.PcrValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


@ExtendWith(MockitoExtension.class)
class PcrClientTest {
    private static final String PCR_URL = "http://localhost:8080/api/mock/pcr/extract";
    private static final String VALID_SSN = "987654-321";

    private MockRestServiceServer mockServer;
    private PcrClient pcrClient;


    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();

        pcrClient = new PcrClient(builder);

        // Устанавливаем @Value-поля через ReflectionTestUtils
        ReflectionTestUtils.setField(pcrClient, "apiUrl", PCR_URL);
        ReflectionTestUtils.setField(pcrClient, "ownerIdCodeType", "BusinessId");
        ReflectionTestUtils.setField(pcrClient, "ownerIdCode", "12345-6789");
        ReflectionTestUtils.setField(pcrClient, "ownerCountryCode", "LT");
        ReflectionTestUtils.setField(pcrClient, "targetEnvironment", "Test");
    }
    @Test
    void shouldReturnPcrResponse_whenApiRespondsSuccessfully() {
        String responseJson = """
                 {
                   "statusMessage": "The request succeeded",
                   "creditRegisterExtract": {
                     "extractReference": "GU-1319",
                     "creationTimeUtc": "2026-09-07T09:48:00",
                     "personRequested": {
                       "idCodeType": "PersonalIdentityCode",
                       "idCode": "987654-321"
                     },
                     "voluntaryBanOnCredits": {
                       "isInEffect": false
                     }
                   }
                 }

                """;

        mockServer.expect(requestTo(PCR_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        PcrResponse response = pcrClient.fetchCreditData(VALID_SSN);

        assertThat(response).isNotNull();
        assertThat(response.getStatusMessage()).isEqualTo("The request succeeded");
        assertThat(response.getCreditRegisterExtract()).isNotNull();
        assertThat(response.getCreditRegisterExtract().getExtractReference()).isEqualTo("GU-1319");
        assertThat(response.getErrorResponses()).isNull();

        mockServer.verify();
    }

    @Test
    void fetchCreditData_clientError() {
        mockServer.expect(requestTo(PCR_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest()
                        .body("{\"error\":\"bad request\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> pcrClient.fetchCreditData(VALID_SSN))
                .isInstanceOf(PcrValidationException.class)
                .hasMessageContaining("PCR rejected request");

        mockServer.verify();
    }

}