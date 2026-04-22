package com.sanos.mediaservice.controller;

import com.sanos.mediaservice.dto.MediaDto;
import com.sanos.mediaservice.model.FotografiaMascota;
import com.sanos.mediaservice.repository.FotografiaMascotaRepository;
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
public class MediaController {

    private final FotografiaMascotaRepository repo;

    public MediaController(FotografiaMascotaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<MediaDto> list() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

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

    @GetMapping("/pet/{petId}")
    public List<MediaDto> byPet(@PathVariable Long petId) {
        return repo.findByIdMascota(petId).stream().map(this::toDto).toList();
    }

    @GetMapping("/report/{reportId}")
    public List<MediaDto> byReport(@PathVariable Long reportId) {
        return repo.findByIdReporte(reportId).stream().map(this::toDto).toList();
    }

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
