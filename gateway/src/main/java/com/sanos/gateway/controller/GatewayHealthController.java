package com.sanos.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/gateway")
public class GatewayHealthController {

    @GetMapping("/health")
    public Mono<Map<String, String>> health() {
        return Mono.just(Map.of("status", "UP", "service", "gateway"));
    }
}
