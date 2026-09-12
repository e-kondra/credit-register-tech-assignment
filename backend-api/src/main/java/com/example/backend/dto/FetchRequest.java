package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FetchRequest {

    @NotBlank(message = "SSN must not be blank")
    @Size(min = 5, max = 20, message = "SSN length must be between 5 and 20 characters")
    private String ssn;
}
