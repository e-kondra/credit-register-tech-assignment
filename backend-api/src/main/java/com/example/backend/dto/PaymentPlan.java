package com.example.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PaymentPlan {
    private boolean isInDebtArrangement;
    private boolean isInBusinessRestructuringProgram;
}
