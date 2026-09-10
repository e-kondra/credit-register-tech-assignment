package com.example.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class RequestedPerson {
    private String idCodeType;
    private String idCode;
}
