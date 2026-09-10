package com.example.backend.controller;

import com.example.backend.dto.FetchRequest;
import com.example.backend.entity.CreditExtract;
import com.example.backend.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/credit")
public class CreditController {
    private final CreditService creditService;

    @PostMapping("/fetch")
    public ResponseEntity<CreditExtract> fetchCredit(@RequestBody FetchRequest request) {
        System.out.println("fetch");
        CreditExtract saved = creditService.fetchAndSave(request.getSsn());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/history/{ssn}")
    public ResponseEntity<List<CreditExtract>> getHistory(@PathVariable String ssn) {
        List<CreditExtract> history = creditService.getHistory(ssn);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<CreditExtract> getDetails(@PathVariable Long id) {
        CreditExtract details = creditService.getDetails(id);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/internal/credit-bans")
    public ResponseEntity<List<CreditExtract>> getCreditBans() {
        List<CreditExtract> bans = creditService.getCreditBans();
        return ResponseEntity.ok(bans);
    }
}
