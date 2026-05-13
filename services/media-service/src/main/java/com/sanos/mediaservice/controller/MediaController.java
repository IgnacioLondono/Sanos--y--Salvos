package com.sanos.mediaservice.controller;

import com.sanos.mediaservice.dto.MediaDto;
import com.sanos.mediaservice.model.FotografiaMascota;
import com.sanos.mediaservice.repository.FotografiaMascotaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
@CrossOrigin(origins = "*")
@Tag(name = "Media", description = "Fotografias. Tabla: fotografias_mascotas (db_media).")
public class MediaController {

    private final FotografiaMascotaRepository repo;

    public MediaController(FotografiaMascotaRepository repo) {
        this.repo = repo;
    }

    @Operation(summary = "Listar todas las fotos")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = MediaDto.class)))
    @GetMapping
    public List<MediaDto> list() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @Operation(summary = "Subir registro multimedia", description = "Crea fila con url, tags y fecha.")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = MediaDto.class)))
    @PostMapping
    public ResponseEntity<MediaDto> upload(@RequestBody MediaDto req) {
        FotografiaMascota f = new FotografiaMascota();
        f.setIdMascota(req.petId());
        f.setIdReporte(req.reportId());
        f.setUrlAlmacenamiento(req.url());
        f.setTags(req.tags() == null ? "" : String.join(",", req.tags()));
        f.setFechaCaptura(req.takenAt() != null ? parseDate(req.takenAt()) : LocalDateTime.now());
        f = repo.save(f);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(f));
    }

    @Operation(summary = "Fotos por mascota")
    @GetMapping("/pet/{petId}")
    public List<MediaDto> byPet(
            @Parameter(description = "id_mascota", required = true) @PathVariable Long petId) {
        return repo.findByIdMascota(petId).stream().map(this::toDto).toList();
    }

    @Operation(summary = "Fotos por reporte")
    @GetMapping("/report/{reportId}")
    public List<MediaDto> byReport(
            @Parameter(description = "id_reporte", required = true) @PathVariable Long reportId) {
        return repo.findByIdReporte(reportId).stream().map(this::toDto).toList();
    }

    @Operation(summary = "Salud del servicio")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "media-service");
    }

    private MediaDto toDto(FotografiaMascota f) {
        List<String> tags = f.getTags() == null || f.getTags().isBlank()
                ? List.of()
                : Arrays.stream(f.getTags().split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        return new MediaDto(
                f.getIdFoto(),
                f.getIdMascota(),
                f.getIdReporte(),
                f.getUrlAlmacenamiento(),
                tags,
                f.getFechaCaptura() != null ? f.getFechaCaptura().toString() : null
        );
    }

    private LocalDateTime parseDate(String iso) {
        try {
            return LocalDateTime.parse(iso.replace("Z", ""));
        } catch (Exception ex) {
            return LocalDateTime.now();
        }
    }
}
