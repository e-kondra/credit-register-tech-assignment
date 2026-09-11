package com.example.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreditBanEvent {
    private String ssn;
    private String extractReference;
    private String reason;
    private LocalDateTime fetchDate;
    private LocalDateTime eventTime;
}
