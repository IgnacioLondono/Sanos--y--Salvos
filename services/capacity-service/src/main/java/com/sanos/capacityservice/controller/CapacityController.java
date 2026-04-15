package com.sanos.capacityservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/capacity")
public class CapacityController {

    private final Map<String, Map<String, Object>> store = new ConcurrentHashMap<>();

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "service", "capacity-service",
                "status", "UP",
                "timestamp", Instant.now().toString(),
                "fields", "id,organization,volunteers,hoursAvailable,zone,availableFrom"
        );
    }

    @GetMapping
    public List<Map<String, Object>> findAll() {
        return new ArrayList<>(store.values());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable String id) {
        Map<String, Object> item = store.get(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        String id = UUID.randomUUID().toString();
        body.put("id", id);
        body.putIfAbsent("createdAt", Instant.now().toString());
        store.put(id, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        if (!store.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        body.put("id", id);
        body.put("updatedAt", Instant.now().toString());
        store.put(id, body);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (store.remove(id) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sample-payload")
    public Map<String, Object> sample() {
        return Map.of("example", "Use POST to create records for this service");
    }
}
