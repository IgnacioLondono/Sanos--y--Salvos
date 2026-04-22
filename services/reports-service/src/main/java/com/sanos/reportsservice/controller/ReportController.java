package com.sanos.reportsservice.controller;

import com.sanos.reportsservice.dto.ReportDto;
import com.sanos.reportsservice.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping
    public List<ReportDto> listAll() { return service.listAll(); }

    @PostMapping
    public ResponseEntity<ReportDto> create(@RequestBody ReportDto req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportDto> byId(@PathVariable Long id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pet/{petId}")
    public List<ReportDto> byPet(@PathVariable Long petId) { return service.findByPet(petId); }

    @GetMapping("/status/{status}")
    public List<ReportDto> byStatus(@PathVariable String status) { return service.findByStatus(status); }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReportDto> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String newStatus = payload.getOrDefault("status", "ABIERTO");
        return service.updateStatus(id, newStatus)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "reports-service");
    }
}
