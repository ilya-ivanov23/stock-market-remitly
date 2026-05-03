package com.remitly.stockmarket.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChaosController {

    @PostMapping("/chaos")
    public void triggerChaos() {
        // Kills the current Spring Boot instance (so Nginx routes requests to the second instance)
        System.exit(1);
    }
}
