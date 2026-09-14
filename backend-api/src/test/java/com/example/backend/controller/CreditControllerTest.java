package com.example.backend.controller;

import com.example.backend.dto.FetchRequest;
import com.example.backend.entity.CreditExtract;
import com.example.backend.entity.CreditExtractStatus;
import com.example.backend.exception.CreditExtractNotFoundException;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.service.CreditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CreditControllerTest {
    @InjectMocks
    CreditController creditController;
    @Mock
    private CreditService creditService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(creditController)
                .setControllerAdvice(GlobalExceptionHandler.class)
                .build();
    }

    @Test
    void givenCorrectSsnFetch_success() throws Exception{
        CreditExtract saved = buildExtract(1L, "987654-321", CreditExtractStatus.SUCCESS, false);
        when(creditService.fetchAndSave("987654-321")).thenReturn(saved);

        FetchRequest request = new FetchRequest();
        request.setSsn("987654-321");

        mockMvc.perform(post("/api/credit/fetch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ssn").value("987654-321"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.voluntaryCreditBan").value(false));

        verify(creditService).fetchAndSave("987654-321");
    }

    @Test
    void givenEmptySsnFetch_validationError() throws Exception {
        FetchRequest request = new FetchRequest();
        request.setSsn("");

        mockMvc.perform(post("/api/credit/fetch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(creditService);
    }

    @Test
    void givenSsnGetHistory_success() throws Exception {
        List<CreditExtract> history = List.of(
                buildExtract(1L, "987654-321", CreditExtractStatus.SUCCESS, false),
                buildExtract(2L, "987654-321", CreditExtractStatus.SUCCESS, true)
        );
        when(creditService.getHistory("987654-321")).thenReturn(history);

        mockMvc.perform(get("/api/credit/history/987654-321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(creditService).getHistory("987654-321");
    }

    @Test
    void givenIncorrectSsnGetHistory_empty() throws Exception {
        when(creditService.getHistory("111111-111")).thenReturn(List.of());

        mockMvc.perform(get("/api/credit/history/111111-111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getDetails_success() throws Exception {
        CreditExtract extract = buildExtract(42L, "987654-321", CreditExtractStatus.SUCCESS, false);
        when(creditService.getDetails(42L)).thenReturn(extract);

        mockMvc.perform(get("/api/credit/details/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.ssn").value("987654-321"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(creditService).getDetails(42L);
    }

    @Test
    void getDetails_notFound() throws Exception {
        when(creditService.getDetails(999L))
                .thenThrow(new CreditExtractNotFoundException(999L));

        mockMvc.perform(get("/api/credit/details/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CREDIT_EXTRACT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Credit extract not found with ID: 999"));
    }

    @Test
    void givenSsnWithBanGetCreditBans_success() throws Exception {
        List<CreditExtract> bans = List.of(
                buildExtract(1L, "777777-777", CreditExtractStatus.SUCCESS, true)
        );
        when(creditService.getCreditBans()).thenReturn(bans);

        mockMvc.perform(get("/api/credit/internal/credit-bans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ssn").value("777777-777"))
                .andExpect(jsonPath("$[0].voluntaryCreditBan").value(true));

        verify(creditService).getCreditBans();
    }

    private CreditExtract buildExtract(Long id, String ssn, CreditExtractStatus status, boolean ban) {
        return CreditExtract.builder()
                .id(id)
                .ssn(ssn)
                .fetchDate(LocalDateTime.of(2026, 9, 12, 10, 0))
                .extractReference("GU-" + id)
                .status(status)
                .voluntaryCreditBan(ban)
                .banReason(ban ? "ControlOfPersonalFinances" : null)
                .lendersCount(1)
                .loanContractsCount(2)
                .totalLoanAmount(BigDecimal.valueOf(24000.00))
                .currencyCode("EUR")
                .fullResponse("{\"statusMessage\":\"ok\"}")
                .build();
    }

}