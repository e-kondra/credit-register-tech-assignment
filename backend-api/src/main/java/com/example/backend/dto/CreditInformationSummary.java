package com.example.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Getter
@Setter
public class CreditInformationSummary {
        private Integer lendersCount;
        private Integer loanContractsCount;
        private Integer guaranteedLoanContractsCount;
        private List<BigDecimal> repaymentsPaidLastAmount;
        private String currencyCode;
        private BigDecimal sum;
}
