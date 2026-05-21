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
import com.sanos.mediaservice.config.MediaStorageProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@CrossOrigin(origins = "*")
@Tag(name = "Media", description = "Fotografias. Tabla: fotografias_mascotas (db_media).")
public class MediaController {

    private final FotografiaMascotaRepository repo;
    private final MediaStorageProperties storageProperties;

    public MediaController(FotografiaMascotaRepository repo, MediaStorageProperties storageProperties) {
        this.repo = repo;
        this.storageProperties = storageProperties;
    }

    @Operation(summary = "Listar todas las fotos")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = MediaDto.class)))
    @GetMapping
    public List<MediaDto> list() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @Operation(summary = "Subir archivo de imagen", description = "Multipart: file, petId, reportId (opcionales), tags.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "petId", required = false) Long petId,
            @RequestParam(value = "reportId", required = false) Long reportId,
            @RequestParam(value = "tags", required = false) String tags) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Archivo requerido"));
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Solo se permiten imagenes"));
        }
        try {
            Path dir = Path.of(storageProperties.getUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String ext = extensionFrom(file.getOriginalFilename(), contentType);
            String storedName = UUID.randomUUID() + ext;
            Path target = dir.resolve(storedName);
            Files.write(target, file.getBytes());

            String publicUrl = storageProperties.getPublicBasePath() + "/" + storedName;
            FotografiaMascota f = new FotografiaMascota();
            f.setIdMascota(petId);
            f.setIdReporte(reportId);
            f.setUrlAlmacenamiento(publicUrl);
            f.setTags(tags == null ? "" : tags.trim());
            f.setFechaCaptura(LocalDateTime.now());
            f = repo.save(f);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(f));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo guardar el archivo"));
        }
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

    private String extensionFrom(String originalName, String contentType) {
        if (originalName != null && originalName.contains(".")) {
            String ext = originalName.substring(originalName.lastIndexOf('.')).toLowerCase();
            if (ext.matches("\\.(jpg|jpeg|png|gif|webp)")) {
                return ext;
            }
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
