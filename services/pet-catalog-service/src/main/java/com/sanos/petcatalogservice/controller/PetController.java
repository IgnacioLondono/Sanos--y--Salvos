package com.sanos.petcatalogservice.controller;

import com.sanos.petcatalogservice.dto.PetDto;
import com.sanos.petcatalogservice.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pets")
@CrossOrigin(origins = "*")
@Tag(name = "Catalogo mascotas", description = "CRUD mascotas. Tablas: mascotas, caracteristicas_fisicas, vinculos_mascotas (db_pets).")
public class PetController {

    private final PetService service;

    public PetController(PetService service) {
        this.service = service;
    }

    @Operation(summary = "Listar mascotas", description = "Todas las mascotas con DTO agregado.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PetDto.class)))
    @GetMapping
    public List<PetDto> list() { return service.listAll(); }

    @Operation(summary = "Crear mascota", description = "Inserta mascota, caracteristicas y vinculo dueno.")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = PetDto.class)))
    @PostMapping
    public ResponseEntity<PetDto> create(@RequestBody PetDto req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @Operation(summary = "Mascota por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PetDto.class))),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PetDto> byId(
            @Parameter(description = "mascotas.id_mascota", required = true) @PathVariable Long id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar mascota", description = "Borra registro y datos asociados segun logica del servicio.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminada"),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "mascotas.id_mascota", required = true) @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar por chip", description = "Lookup por numero_chip unico.")
    @GetMapping("/by-chip/{chip}")
    public ResponseEntity<PetDto> byChip(
            @Parameter(description = "mascotas.numero_chip", example = "CHIP-001", required = true) @PathVariable String chip) {
        return service.findByChip(chip).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Mascotas por dueno", description = "Filtra por vinculos_mascotas.id_usuario.")
    @GetMapping("/owner/{ownerId}")
    public List<PetDto> byOwner(
            @Parameter(description = "id_usuario dueno", required = true) @PathVariable Long ownerId) {
        return service.findByOwner(ownerId);
    }

    @Operation(summary = "Salud del servicio")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "pet-catalog-service");
    }
}
