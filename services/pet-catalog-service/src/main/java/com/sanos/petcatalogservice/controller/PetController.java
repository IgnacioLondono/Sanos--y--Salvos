package com.sanos.petcatalogservice.controller;

import com.sanos.petcatalogservice.dto.PetDto;
import com.sanos.petcatalogservice.service.PetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pets")
@CrossOrigin(origins = "*")
public class PetController {

    private final PetService service;

    public PetController(PetService service) {
        this.service = service;
    }

    @GetMapping
    public List<PetDto> list() { return service.listAll(); }

    @PostMapping
    public ResponseEntity<PetDto> create(@RequestBody PetDto req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetDto> byId(@PathVariable Long id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-chip/{chip}")
    public ResponseEntity<PetDto> byChip(@PathVariable String chip) {
        return service.findByChip(chip).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/{ownerId}")
    public List<PetDto> byOwner(@PathVariable Long ownerId) {
        return service.findByOwner(ownerId);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "pet-catalog-service");
    }
}
