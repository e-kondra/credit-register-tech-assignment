package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditBanEvent {

    private String ssn;
    private String extractReference;
    private String reason;
    private LocalDateTime fetchDate;
    private LocalDateTime eventTime;

}
