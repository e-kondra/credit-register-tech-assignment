package com.example.backend.service;

import com.example.backend.client.PcrClient;
import com.example.backend.dto.CreditInformationSummary;
import com.example.backend.dto.CreditRegisterExtract;
import com.example.backend.dto.PcrResponse;
import com.example.backend.dto.VoluntaryBanOnCredits;
import com.example.backend.entity.CreditExtract;
import com.example.backend.entity.CreditExtractStatus;
import com.example.backend.exception.CreditExtractNotFoundException;
import com.example.backend.exception.InvalidSsnException;
import com.example.backend.exception.PcrUnavailableException;
import com.example.backend.repository.CreditExtractRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditService {
    private final PcrClient pcrClient;
    private final CreditExtractRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaMessageSender kafkaMessageSender;

    // SSN: DDMMYY-NNNN or DDMMYY+NNNN
    private static final Pattern SSN_PATTERN = Pattern.compile("^\\d{6}[-+]\\d{3,4}$");

    public CreditExtract fetchAndSave(String ssn) {
        validateSsn(ssn);
        log.info("Fetching credit data for SSN: {}", ssn);

        PcrResponse response;
        try {
            response = pcrClient.fetchCreditData(ssn);
        } catch (PcrUnavailableException exception) {
            // PCR unavailable — save SERVICE_ERROR
            saveErrorRecord(ssn, null, CreditExtractStatus.SERVICE_ERROR, exception.getMessage());
            throw exception;
        }

        if (hasErrorResponses(response)){
            CreditExtract saved = saveErrorRecord(ssn, response, CreditExtractStatus.PCR_ERROR, null);
            log.warn("PCR returned validation error for SSN {}: saved as PCR_ERROR with ID {}",
                    ssn, saved.getId());
            return saved;
        }

        if (isDeceased(response)) {
            CreditExtract saved = saveDeceased(ssn, response);
            log.warn("Client is deceased for SSN {}: saved as DECEASED with ID {}", ssn, saved.getId());
            return saved;
        }

        //success response
        CreditExtract saved = saveSuccess(ssn, response);

        // If it has voluntary ban, sent event to Kafka
        if (saved.isVoluntaryCreditBan()) {
            kafkaMessageSender.sendCreditBanEvent(saved);
        }
        return saved;
    }

    private boolean hasErrorResponses(PcrResponse response) {
        return response.getErrorResponses() != null && !response.getErrorResponses().isEmpty();
    }

    private boolean isDeceased(PcrResponse response) {
        return response.getCreditRegisterExtract() != null
                && response.getCreditRegisterExtract().getDeceasedPerson() != null;
    }

    private CreditExtract saveSuccess(String ssn, PcrResponse response) {
        CreditRegisterExtract extract = response.getCreditRegisterExtract();

        CreditExtract creditExtract = CreditExtract.builder()
                .ssn(ssn)
                .fetchDate(LocalDateTime.now())
                .status(CreditExtractStatus.SUCCESS)
                .extractReference(extract.getExtractReference())
                .build();

        // voluntary ban
        VoluntaryBanOnCredits ban = extract.getVoluntaryBanOnCredits();
        if (ban != null) {
            creditExtract.setVoluntaryCreditBan(ban.isInEffect());
            creditExtract.setBanReason(ban.getReason());
        }

        // summary
        CreditInformationSummary summary = extract.getCreditInformationSummary();
        if (summary != null) {
            creditExtract.setLendersCount(summary.getLendersCount());
            creditExtract.setLoanContractsCount(summary.getLoanContractsCount());
            creditExtract.setTotalLoanAmount(summary.getSum());
            creditExtract.setCurrencyCode(summary.getCurrencyCode());
        }

        // Full JSON
        creditExtract.setFullResponse(serialize(response));
        return repository.save(creditExtract);
    }

    private CreditExtract saveErrorRecord(String ssn, PcrResponse response,
                                          CreditExtractStatus status, String errorMessage) {
        CreditExtract creditExtract = CreditExtract.builder()
                .ssn(ssn)
                .fetchDate(LocalDateTime.now())
                .status(status)
                .extractReference(response != null && response.getCreditRegisterExtract() != null
                        ? response.getCreditRegisterExtract().getExtractReference()
                        : null)
                .fullResponse(response != null
                        ? serialize(response)
                        : "{\"error\":\"" + errorMessage + "\"}")
                .build();

        return repository.save(creditExtract);
    }

    private CreditExtract saveDeceased(String ssn, PcrResponse response) {
        CreditRegisterExtract extract = response.getCreditRegisterExtract();

        CreditExtract creditExtract = CreditExtract.builder()
                .ssn(ssn)
                .fetchDate(LocalDateTime.now())
                .status(CreditExtractStatus.DECEASED)
                .extractReference(extract.getExtractReference())
                .voluntaryCreditBan(false)
                .fullResponse(serialize(response))
                .build();

        return repository.save(creditExtract);
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize response", e);
            return "{}";
        }
    }

    private void validateSsn(String ssn) {
        if (ssn == null || ssn.isBlank()) {
            throw new InvalidSsnException("SSN must not be empty");
        }
        if (!SSN_PATTERN.matcher(ssn).matches()) {
            throw new InvalidSsnException(
                    "SSN must match format DDMMYY-NNNN (e.g. 987654-321). Got: " + ssn);
        }
    }

    public List<CreditExtract> getHistory(String ssn) {
        validateSsn(ssn);
        return repository.findBySsnOrderByFetchDateDesc(ssn);
    }

    public CreditExtract getDetails(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CreditExtractNotFoundException(id));
    }

    public List<CreditExtract> getCreditBans() {
        return repository.findByVoluntaryCreditBanTrue();
    }
}
