package com.example.backend.controller;


import com.example.backend.dto.BusinessInformation;
import com.example.backend.dto.CreditInformationSummary;
import com.example.backend.dto.CreditRegisterExtract;
import com.example.backend.dto.DeceasedPerson;
import com.example.backend.dto.DefermentPeriod;
import com.example.backend.dto.ErrorResponse;
import com.example.backend.dto.IncomeData;
import com.example.backend.dto.LoanInfo;
import com.example.backend.dto.Months;
import com.example.backend.dto.PaymentPlan;
import com.example.backend.dto.PcrRequest;
import com.example.backend.dto.PcrResponse;

import com.example.backend.dto.RequestedPerson;
import com.example.backend.dto.VoluntaryBanOnCredits;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/mock/pcr")
public class MockPositiveCreditRegistryController {

    @PostMapping("/extract")
    public PcrResponse getCreditExtract(@RequestBody PcrRequest request){
        String ssn = request.getRequest().getIdCode();

        // For demo:
        // if ssn contains "999", return error
        if (ssn.contains("999")) {
            return PcrResponse.builder()
                    .statusMessage("Validation failed")
                    .errorResponses(List.of(
                            ErrorResponse.builder()
                                    .fieldName("idCode")
                                    .errorCode("C40")
                                    .errorDescription("Invalid ID code format")
                                    .build()
                    ))
                    .build();
        }
        // if SSN contains "888", return deceased person
        if (ssn.contains("888")) {
            return PcrResponse.builder()
                    .statusMessage("The request succeeded")
                    .creditRegisterExtract(CreditRegisterExtract.builder()
                            .extractReference("GU-" + System.currentTimeMillis())
                            .creationTimeUtc(LocalDateTime.now())
                            .requestedPerson(RequestedPerson.builder()
                                    .idCodeType("PersonalIdentityCode")
                                    .idCode(ssn)
                                    .build())
                            .owner(PcrRequest.OwnerInfo.builder()
                                    .idCodeType("BusinessId")
                                    .idCode("12345-6789")
                                    .countryCode("LT")
                                    .build())
                            .deceasedPerson(DeceasedPerson.builder()
                                    .dateOfDeath(LocalDate.now().minusDays(180))
                                    .build())
                            .build())
                    .build();
        }

        // if SSN contains "777" return success response with voluntary Ban
        VoluntaryBanOnCredits voluntaryBan = null;
        if (ssn.contains("777")) {
            voluntaryBan = VoluntaryBanOnCredits.builder()
                    .isInEffect(true)
                    .reason("ControlOfPersonalFinances")
                    .build();
        } else {
            voluntaryBan = VoluntaryBanOnCredits.builder()
                    .isInEffect(false)
                    .build();
        }

        System.out.println("voluntaryBan = " + voluntaryBan.isInEffect() + ", reason = "+ voluntaryBan.getReason());
        return PcrResponse.builder()
                .statusMessage("The request succeeded")
                .creditRegisterExtract(CreditRegisterExtract.builder()
                        .extractReference("GU-" + System.currentTimeMillis())
                        .creationTimeUtc(LocalDateTime.now())
                        .requestedPerson(RequestedPerson.builder()
                                .idCodeType("PersonalIdentityCode")
                                .idCode(ssn)
                                .build())
                        .owner(PcrRequest.OwnerInfo.builder()
                                .idCodeType("BusinessId")
                                .idCode("12345-6789")
                                .countryCode("LT")
                                .build())
                        .voluntaryBanOnCredits(voluntaryBan)
                        .creditInformationSummary(CreditInformationSummary.builder()
                                .lendersCount(1)
                                .loanContractsCount(2)
                                .guaranteedLoanContractsCount(1)
                                .repaymentsPaidLastAmount(List.of(BigDecimal.valueOf(12000.00), BigDecimal.valueOf(12000.00)))
                                .currencyCode("EUR")
                                .sum(BigDecimal.valueOf(24000.00))
                                .build())
                        .loans(List.of(
                                LoanInfo.builder()
                                        .loanType("LumpSumLoan")
                                        .contractDate(LocalDate.of(2024, 11, 7))
                                        .isLoanWithCollateral(true)
                                        .collateralType(List.of("ApartmentOrRealEstate"))
                                        .borrowersCount(1)
                                        .currencyCode("EUR")
                                        .businessInformation(BusinessInformation.builder()
                                                .borrowerBusinessID("123654-789")
                                                .borrowerBusinessName("SomeBusiness")
                                                .build())
                                        .defermentPeriods(List.of(
                                                DefermentPeriod.builder()
                                                        .startDate(LocalDate.of(2025, 9, 7))
                                                        .endDate(LocalDate.of(2025, 10, 7))
                                                        .build()
                                        ))
                                        .paymentPlan(PaymentPlan.builder()
                                                .isInDebtArrangement(true)
                                                .isInBusinessRestructuringProgram(true)
                                                .build())
                                        .accuracyIsDenied(true)
                                        .build()
                        ))
                        .incomeData(List.of(
                                IncomeData.builder()
                                        .year(2025)
                                        .months(List.of(
                                                Months.builder()
                                                        .month(1)
                                                        .wagesGrossAmount(BigDecimal.valueOf(1000.00))
                                                        .wagesNetAmount(BigDecimal.valueOf(860.00))
                                                        .benefitsGrossAmount(BigDecimal.valueOf(100.00))
                                                        .benefitsNetAmount(BigDecimal.valueOf(20.00))
                                                        .build(),
                                                Months.builder()
                                                        .month(2)
                                                        .wagesGrossAmount(BigDecimal.valueOf(1000.00))
                                                        .wagesNetAmount(BigDecimal.valueOf(860.00))
                                                        .benefitsGrossAmount(BigDecimal.valueOf(100.00))
                                                        .benefitsNetAmount(BigDecimal.valueOf(20.00))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build())
                .build();
    }
}
