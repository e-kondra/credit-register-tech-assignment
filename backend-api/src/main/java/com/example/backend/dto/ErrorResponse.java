package com.example.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ErrorResponse {
        private String fieldName;
        private String errorCode;
        private String errorDescription;
}
