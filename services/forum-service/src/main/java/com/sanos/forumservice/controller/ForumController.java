package com.sanos.forumservice.controller;

import com.sanos.forumservice.dto.*;
import com.sanos.forumservice.service.ForumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
@CrossOrigin(origins = "*")
@Tag(name = "Foro", description = "Hilos y mensajes de la comunidad. BD: db_foro. Tablas: hilos_foro, mensajes_foro.")
public class ForumController {

    private final ForumService service;

    public ForumController(ForumService service) {
        this.service = service;
    }

    @Operation(
            summary = "Listar hilos del foro",
            description = "Devuelve hilos ordenados por fecha de actualizacion. Filtro opcional por categoria (AYUDA, CONSEJOS, GENERAL). Lectura publica via gateway."
    )
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ThreadDto.class)))
    @GetMapping("/threads")
    public List<ThreadDto> listThreads(
            @Parameter(description = "Categoria: AYUDA | CONSEJOS | GENERAL")
            @RequestParam(required = false) String category) {
        return service.listThreads(category);
    }

    @Operation(summary = "Detalle de hilo con todos los mensajes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ThreadDetailDto.class))),
            @ApiResponse(responseCode = "404", description = "Hilo no encontrado")
    })
    @GetMapping("/threads/{id}")
    public ResponseEntity<ThreadDetailDto> threadById(
            @Parameter(description = "id_hilo", required = true) @PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.getThread(id));
        } catch (IllegalStateException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Crear nuevo hilo",
            description = "Crea registro en hilos_foro y el primer mensaje en mensajes_foro. Requiere JWT en gateway."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = ThreadDetailDto.class))),
            @ApiResponse(responseCode = "400", description = "Validacion (titulo o mensaje corto)")
    })
    @SecurityRequirement(name = "bearer-jwt")
    @PostMapping("/threads")
    public ResponseEntity<?> createThread(@RequestBody CreateThreadRequest body) {
        try {
            ThreadDetailDto created = service.createThread(body);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(summary = "Responder en un hilo", description = "Inserta fila en mensajes_foro y actualiza fecha_actualizacion del hilo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = PostDto.class))),
            @ApiResponse(responseCode = "400", description = "Mensaje vacio"),
            @ApiResponse(responseCode = "404", description = "Hilo no encontrado")
    })
    @SecurityRequirement(name = "bearer-jwt")
    @PostMapping("/threads/{id}/posts")
    public ResponseEntity<?> addPost(
            @Parameter(description = "id_hilo", required = true) @PathVariable Long id,
            @RequestBody CreatePostRequest body) {
        try {
            PostDto post = service.addPost(id, body);
            return ResponseEntity.status(HttpStatus.CREATED).body(post);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(summary = "Salud del servicio", description = "Endpoint publico para healthchecks y panel admin.")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "forum-service");
    }
}
