package com.example.backend.service;

import com.example.backend.client.PcrClient;
import com.example.backend.dto.CreditInformationSummary;
import com.example.backend.dto.CreditRegisterExtract;
import com.example.backend.dto.PcrResponse;
import com.example.backend.dto.VoluntaryBanOnCredits;
import com.example.backend.entity.CreditExtract;
import com.example.backend.repository.CreditExtractRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditService {
    private final PcrClient pcrClient;
    private final CreditExtractRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaMessageSender kafkaMessageSender;

    @Transactional
    public CreditExtract fetchAndSave(String ssn) {
        log.info("Fetching credit data for SSN: {}", ssn);

        // 1. call PCR API
        PcrResponse response = pcrClient.fetchCreditData(ssn);

        // 2. Map to CreditExtract entity
        CreditExtract extract = mapToEntity(ssn, response);

        // 3. Saving data
        CreditExtract saved = repository.save(extract);
        log.info("Saved credit extract with ID: {}", saved.getId());

        // 4. If it has voluntary ban, sent event to Kafka
        if (saved.isVoluntaryCreditBan()) {
            kafkaMessageSender.sendCreditBanEvent(saved);
        }
        return saved;
    }

    private CreditExtract mapToEntity(String ssn, PcrResponse response) {
        CreditRegisterExtract extract = response.getCreditRegisterExtract();

        CreditExtract entity = CreditExtract.builder()
                .ssn(ssn)
                .fetchDate(LocalDateTime.now())
                .extractReference(extract.getExtractReference())
                .build();

        // Voluntary ban
        VoluntaryBanOnCredits ban = extract.getVoluntaryBanOnCredits();
        if (ban != null) {
            entity.setVoluntaryCreditBan(ban.isInEffect());
            entity.setBanReason(ban.getReason());
        }

        // Summary
        CreditInformationSummary summary = extract.getCreditInformationSummary();
        if (summary != null) {
            entity.setLendersCount(summary.getLendersCount());
            entity.setLoanContractsCount(summary.getLoanContractsCount());
            entity.setTotalLoanAmount(summary.getSum());
            entity.setCurrencyCode(summary.getCurrencyCode());
        }

        // Full JSON
        try {
            entity.setFullResponse(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            log.error("Failed to serialize response to JSON", e);
        }

        return entity;
    }

    public List<CreditExtract> getHistory(String ssn) {
        return repository.findBySsnOrderByFetchDateDesc(ssn);
    }

    public CreditExtract getDetails(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credit extract not found with ID: " + id));
    }

    public List<CreditExtract> getCreditBans() {
        return repository.findByVoluntaryCreditBanTrue();
    }
}
