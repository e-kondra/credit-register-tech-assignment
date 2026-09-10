package com.example.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Builder
@Getter
@Setter
public class DefermentPeriod {
    private LocalDate startDate;
    private LocalDate endDate;
}
