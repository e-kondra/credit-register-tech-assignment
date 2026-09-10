package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_extracts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditExtract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ssn;

    @Column(nullable = false)
    private LocalDateTime fetchDate;

    private String extractReference;

    @Column(name = "voluntary_credit_ban")
    private boolean voluntaryCreditBan;

    @Column(name = "ban_reason")
    private String banReason;

    private Integer lendersCount;
    private Integer loanContractsCount;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalLoanAmount;

    private String currencyCode;

    @Column(columnDefinition = "TEXT")
    private String fullResponse;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}