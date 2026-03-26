package com.rsvp_backend.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public String health() {
        return "OK";
    }
}
