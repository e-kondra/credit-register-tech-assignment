package com.example.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class IncomeData {
    private int year;
    private List<Months> months;

}
