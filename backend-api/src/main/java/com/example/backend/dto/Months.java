package com.example.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
public class Months {
    private Integer month;
    private BigDecimal wagesGrossAmount;
    private BigDecimal wagesNetAmount;
    private BigDecimal benefitsGrossAmount;
    private BigDecimal benefitsNetAmount;
}
