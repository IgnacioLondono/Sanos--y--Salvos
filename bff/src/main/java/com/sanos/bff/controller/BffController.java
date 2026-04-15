package com.sanos.bff.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bff")
public class BffController {

    private final RestTemplate restTemplate;

    @Value("")
    private String petCatalogUrl;

    @Value("")
    private String mediaUrl;

    @Value("")
    private String reportsUrl;

    @Value("")
    private String capacityUrl;

    public BffController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        List<?> pets = restTemplate.getForObject(petCatalogUrl + "/api/pets", List.class);
        List<?> reports = restTemplate.getForObject(reportsUrl + "/api/reports", List.class);
        List<?> capacity = restTemplate.getForObject(capacityUrl + "/api/capacity", List.class);
        List<?> media = restTemplate.getForObject(mediaUrl + "/api/media", List.class);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalPets", pets != null ? pets.size() : 0);
        result.put("totalReports", reports != null ? reports.size() : 0);
        result.put("totalCapacityRecords", capacity != null ? capacity.size() : 0);
        result.put("totalPhotos", media != null ? media.size() : 0);
        result.put("pets", pets);
        result.put("reports", reports);
        result.put("capacity", capacity);
        result.put("media", media);
        return result;
    }

    @GetMapping("/pet-overview/{petId}")
    public ResponseEntity<Map<String, Object>> petOverview(@PathVariable String petId) {
        List<?> pets = restTemplate.getForObject(petCatalogUrl + "/api/pets", List.class);
        List<?> reports = restTemplate.getForObject(reportsUrl + "/api/reports", List.class);
        List<?> media = restTemplate.getForObject(mediaUrl + "/api/media", List.class);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("petId", petId);
        result.put("pets", pets);
        result.put("reports", reports);
        result.put("media", media);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "component", "bff");
    }
}
