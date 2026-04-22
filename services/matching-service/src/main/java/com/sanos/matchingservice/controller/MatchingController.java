package com.sanos.matchingservice.controller;

import com.sanos.matchingservice.dto.MatchDto;
import com.sanos.matchingservice.service.MatchingEngine;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matching")
@CrossOrigin(origins = "*")
public class MatchingController {

    private final MatchingEngine engine;

    public MatchingController(MatchingEngine engine) {
        this.engine = engine;
    }

    @GetMapping
    public List<MatchDto> list() { return engine.listAll(); }

    @PostMapping("/run")
    public List<MatchDto> run() { return engine.runFullMatching(); }

    @PostMapping
    public MatchDto manualCreate(@RequestBody MatchDto req) { return engine.manualCreate(req); }

    @GetMapping("/report/{reportId}")
    public List<MatchDto> byReport(@PathVariable Long reportId) { return engine.byReport(reportId); }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "matching-service");
    }
}
