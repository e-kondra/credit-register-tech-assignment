package com.example.backend.service;

import com.example.backend.client.PcrClient;
import com.example.backend.dto.CreditInformationSummary;
import com.example.backend.dto.CreditRegisterExtract;
import com.example.backend.dto.DeceasedPerson;
import com.example.backend.dto.ErrorResponse;
import com.example.backend.dto.PcrResponse;
import com.example.backend.dto.RequestedPerson;
import com.example.backend.dto.VoluntaryBanOnCredits;
import com.example.backend.entity.CreditExtract;
import com.example.backend.entity.CreditExtractStatus;
import com.example.backend.exception.CreditExtractNotFoundException;
import com.example.backend.exception.InvalidSsnException;
import com.example.backend.exception.PcrUnavailableException;
import com.example.backend.repository.CreditExtractRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreditServiceTest {
    @Mock
    private PcrClient pcrClient;

    @Mock
    private CreditExtractRepository repository;

    @Mock
    private KafkaMessageSender kafkaMessageSender;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CreditService creditService;

    private static final String VALID_SSN = "987654-321";
    private static final String BAN_SSN = "777777-777";
    private static final String ERROR_SSN = "999999-999";
    private static final String DECEASED_SSN = "888888-888";


    @Test
    void fetchAndSave_success() {
        PcrResponse response = buildSuccessResponse(VALID_SSN, false);
        when(pcrClient.fetchCreditData(VALID_SSN)).thenReturn(response);
        when(repository.save(any(CreditExtract.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreditExtract result = creditService.fetchAndSave(VALID_SSN);

        assertThat(result.getSsn()).isEqualTo(VALID_SSN);
        assertThat(result.getStatus()).isEqualTo(CreditExtractStatus.SUCCESS);
        assertThat(result.isVoluntaryCreditBan()).isFalse();
        assertThat(result.getLendersCount()).isEqualTo(1);
        assertThat(result.getTotalLoanAmount()).isEqualByComparingTo(BigDecimal.valueOf(24000.00));

        verify(repository).save(any(CreditExtract.class));
    }

    @Test
    void fetchAndSave_withBan_publishesToKafka() {
        PcrResponse response = buildSuccessResponse(BAN_SSN, true);
        when(pcrClient.fetchCreditData(BAN_SSN)).thenReturn(response);
        when(repository.save(any(CreditExtract.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreditExtract result = creditService.fetchAndSave(BAN_SSN);

        assertThat(result.getStatus()).isEqualTo(CreditExtractStatus.SUCCESS);
        assertThat(result.isVoluntaryCreditBan()).isTrue();
        assertThat(result.getBanReason()).isEqualTo("ControlOfPersonalFinances");

        verify(repository).save(any(CreditExtract.class));
        verify(kafkaMessageSender).sendCreditBanEvent(result);
    }

    @Test
    void fetchAndSave_pcrError() {

        PcrResponse response = PcrResponse.builder()
                .statusMessage("Validation failed")
                .errorResponses(List.of(
                        ErrorResponse.builder()
                                .fieldName("idCode")
                                .errorCode("C40")
                                .errorDescription("Invalid ID code format")
                                .build()
                ))
                .build();
        when(pcrClient.fetchCreditData(ERROR_SSN)).thenReturn(response);
        when(repository.save(any(CreditExtract.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreditExtract result = creditService.fetchAndSave(ERROR_SSN);

        assertThat(result.getStatus()).isEqualTo(CreditExtractStatus.PCR_ERROR);
        assertThat(result.isVoluntaryCreditBan()).isFalse();
        assertThat(result.getFullResponse()).contains("C40");

        verify(repository).save(any(CreditExtract.class));
        verify(kafkaMessageSender, never()).sendCreditBanEvent(any());
    }

    @Test
    void fetchAndSave_deceased() {
        PcrResponse response = PcrResponse.builder()
                .statusMessage("The request succeeded")
                .creditRegisterExtract(CreditRegisterExtract.builder()
                        .extractReference("GU-1212")
                        .creationTimeUtc(LocalDateTime.now())
                        .deceasedPerson(DeceasedPerson.builder()
                                .dateOfDeath(java.time.LocalDate.of(2024, 8, 29))
                                .build())
                        .build())
                .build();
        when(pcrClient.fetchCreditData(DECEASED_SSN)).thenReturn(response);
        when(repository.save(any(CreditExtract.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreditExtract result = creditService.fetchAndSave(DECEASED_SSN);

        assertThat(result.getStatus()).isEqualTo(CreditExtractStatus.DECEASED);
        assertThat(result.isVoluntaryCreditBan()).isFalse();

        verify(repository).save(any(CreditExtract.class));
    }

    @Test
    void fetchAndSave_pcrUnavailable() {
        when(pcrClient.fetchCreditData(VALID_SSN))
                .thenThrow(new PcrUnavailableException("PCR is down"));
        when(repository.save(any(CreditExtract.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> creditService.fetchAndSave(VALID_SSN))
                .isInstanceOf(PcrUnavailableException.class)
                .hasMessageContaining("PCR is down");
    }

    @Test
    void fetchAndSave_emptySsn() {
        assertThatThrownBy(() -> creditService.fetchAndSave(""))
                .isInstanceOf(InvalidSsnException.class)
                .hasMessageContaining("must not be empty");

        verifyNoInteractions(pcrClient, repository, kafkaMessageSender);
    }

    @Test
    void fetchAndSave_invalidSsnFormat() {
        assertThatThrownBy(() -> creditService.fetchAndSave("abc"))
                .isInstanceOf(InvalidSsnException.class)
                .hasMessageContaining("must match format");

        verifyNoInteractions(pcrClient, repository, kafkaMessageSender);
    }

    @Test
    void getHistory() {
        CreditExtract e1 = CreditExtract.builder().id(1L).ssn(VALID_SSN).build();
        CreditExtract e2 = CreditExtract.builder().id(2L).ssn(VALID_SSN).build();
        when(repository.findBySsnOrderByFetchDateDesc(VALID_SSN)).thenReturn(List.of(e1, e2));

        List<CreditExtract> result = creditService.getHistory(VALID_SSN);

        assertThat(result.size()).isEqualTo(2);
        verify(repository).findBySsnOrderByFetchDateDesc(VALID_SSN);
    }

    @Test
    void getHistory_invalidSsn() {
        assertThatThrownBy(() -> creditService.getHistory("ab"))
                .isInstanceOf(InvalidSsnException.class);
    }

    @Test
    void getDetails_found() {
        CreditExtract entity = CreditExtract.builder().id(42L).ssn(VALID_SSN).build();
        when(repository.findById(42L)).thenReturn(Optional.of(entity));

        CreditExtract result = creditService.getDetails(42L);

        assertThat(result.getId()).isEqualTo(42L);
    }

    @Test
    void getDetails_notFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> creditService.getDetails(999L))
                .isInstanceOf(CreditExtractNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void getCreditBans() {
        CreditExtract ban = CreditExtract.builder().id(1L).voluntaryCreditBan(true).build();
        when(repository.findByVoluntaryCreditBanTrue()).thenReturn(List.of(ban));

        List<CreditExtract> result = creditService.getCreditBans();

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).isVoluntaryCreditBan()).isTrue();
    }


    private PcrResponse buildSuccessResponse(String ssn, boolean hasBan) {
        VoluntaryBanOnCredits ban = VoluntaryBanOnCredits.builder()
                .isInEffect(hasBan)
                .reason(hasBan ? "ControlOfPersonalFinances" : null)
                .build();

        return PcrResponse.builder()
                .statusMessage("The request succeeded")
                .creditRegisterExtract(CreditRegisterExtract.builder()
                        .extractReference("GU-" + System.currentTimeMillis())
                        .creationTimeUtc(LocalDateTime.now())
                        .requestedPerson(RequestedPerson.builder()
                                .idCodeType("PersonalIdentityCode")
                                .idCode(ssn)
                                .build())
                        .voluntaryBanOnCredits(ban)
                        .creditInformationSummary(CreditInformationSummary.builder()
                                .lendersCount(1)
                                .loanContractsCount(2)
                                .currencyCode("EUR")
                                .sum(BigDecimal.valueOf(24000.00))
                                .build())
                        .build())
                .build();
    }
}
