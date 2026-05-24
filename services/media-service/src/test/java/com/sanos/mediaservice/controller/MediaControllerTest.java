package com.sanos.mediaservice.controller;

import com.sanos.mediaservice.config.MediaStorageProperties;
import com.sanos.mediaservice.dto.MediaDto;
import com.sanos.mediaservice.model.FotografiaMascota;
import com.sanos.mediaservice.repository.FotografiaMascotaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

    @Mock
    private FotografiaMascotaRepository repo;
    @Mock
    private MultipartFile file;

    private MediaController controller;

    @BeforeEach
    void setUp() {
        MediaStorageProperties props = new MediaStorageProperties();
        props.setUploadDir("target/test-uploads");
        props.setPublicBasePath("/api/media/files");
        controller = new MediaController(repo, props);
    }

    @Test
    void uploadFile_returnsBadRequestWhenFileIsEmpty() {
        when(file.isEmpty()).thenReturn(true);

        ResponseEntity<?> response = controller.uploadFile(file, 1L, 2L, "tag");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(((Map<?, ?>) response.getBody()).get("error").toString().contains("Archivo"));
    }

    @Test
    void upload_createsMediaRecord() {
        MediaDto req = new MediaDto(null, 7L, 8L, "https://cdn/x.jpg", List.of("a", "b"), "2026-05-24T12:00:00");
        when(repo.save(any(FotografiaMascota.class))).thenAnswer(inv -> {
            FotografiaMascota f = inv.getArgument(0);
            f.setIdFoto(33L);
            f.setFechaCaptura(LocalDateTime.now());
            return f;
        });

        ResponseEntity<MediaDto> response = controller.upload(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(33L, response.getBody().id());
        assertEquals(7L, response.getBody().petId());
        assertEquals(List.of("a", "b"), response.getBody().tags());
    }

    @Test
    void health_returnsUpStatus() {
        Map<String, String> result = controller.health();
        assertEquals("UP", result.get("status"));
        assertEquals("media-service", result.get("service"));
    }
}
