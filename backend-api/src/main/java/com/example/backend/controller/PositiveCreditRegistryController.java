package com.example.backend.controller;


import com.example.backend.dto.PcrRequest;
import com.example.backend.dto.PcrResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mock/pcr")
public class PositiveCreditRegistryController {

    @PostMapping("/extract")
    public PcrResponse getCreditExtract(@RequestBody PcrRequest request){
        return null;
    }
}
