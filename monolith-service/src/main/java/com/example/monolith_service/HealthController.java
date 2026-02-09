package com.example.monolith_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String home() {
        return "Monolith Service is running";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
