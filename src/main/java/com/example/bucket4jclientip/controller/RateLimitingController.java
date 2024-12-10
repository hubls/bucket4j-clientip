package com.example.bucket4jclientip.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class RateLimitingController {
    @GetMapping("/test")
    public String testRateLimit() {
        return "테스트입니다";
    }
}
