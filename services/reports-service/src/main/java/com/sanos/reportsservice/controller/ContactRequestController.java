package com.sanos.reportsservice.controller;

import com.sanos.reportsservice.dto.ContactRequestDto;
import com.sanos.reportsservice.dto.CreateContactRequest;
import com.sanos.reportsservice.dto.RespondContactRequest;
import com.sanos.reportsservice.service.ContactRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports/contact-requests")
@CrossOrigin(origins = "*")
@Tag(name = "Solicitudes de contacto", description = "Mensajeria del mapa: enviar solicitud y aceptar/rechazar.")
public class ContactRequestController {

    private final ContactRequestService service;

    public ContactRequestController(ContactRequestService service) {
        this.service = service;
    }

    @Operation(summary = "Enviar solicitud de contacto", description = "Un ciudadano solicita contactar al dueno de un reporte en el mapa.")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = ContactRequestDto.class)))
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateContactRequest body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(body));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(summary = "Bandeja de entrada", description = "Solicitudes recibidas (pendientes y respondidas).")
    @GetMapping("/inbox")
    public List<ContactRequestDto> inbox(
            @Parameter(description = "id_usuario receptor", required = true) @RequestParam Long userId) {
        return service.inbox(userId);
    }

    @Operation(summary = "Solicitudes enviadas")
    @GetMapping("/sent")
    public List<ContactRequestDto> sent(
            @Parameter(description = "id_usuario emisor", required = true) @RequestParam Long userId) {
        return service.sent(userId);
    }

    @Operation(summary = "Responder solicitud", description = "El receptor acepta (ACCEPTED) o rechaza (REJECTED).")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ContactRequestDto.class)))
    @PatchMapping("/{id}")
    public ResponseEntity<?> respond(
            @Parameter(description = "id_solicitud", required = true) @PathVariable Long id,
            @RequestBody RespondContactRequest body) {
        try {
            return ResponseEntity.ok(service.respond(id, body));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }
}
