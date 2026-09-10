package com.example.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@Setter
public class LoanInfo {
    private String loanType;
    private LocalDate contractDate;
    private boolean isLoanWithCollateral;
    private List<String> collateralType;
    private Integer borrowersCount;
    private String currencyCode;
    private BusinessInformation businessInformation;
    private List<DefermentPeriod> defermentPeriods;
    private PaymentPlan paymentPlan;
    private Boolean accuracyIsDenied;
}
