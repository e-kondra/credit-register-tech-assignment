package com.example.backend.repository;

import com.example.backend.entity.CreditExtract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditExtractRepository extends JpaRepository<CreditExtract, Long> {

    List<CreditExtract> findBySsnOrderByFetchDateDesc(String ssn);

    List<CreditExtract> findByVoluntaryCreditBanTrue();
}
