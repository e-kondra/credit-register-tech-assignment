package com.example.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CreditRegisterExtract {
    private String extractReference;
    private LocalDateTime creationTimeUtc;
    private RequestedPerson requestedPerson;
    private PcrRequest.OwnerInfo owner;
    private VoluntaryBanOnCredits voluntaryBanOnCredits;
    private CreditInformationSummary creditInformationSummary;
    private List<LoanInfo> loans;
    private List<IncomeData> incomeData;
    private DeceasedPerson deceasedPerson;
}
