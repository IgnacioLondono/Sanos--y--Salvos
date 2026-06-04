package com.sanos.reportsservice.controller;

import com.sanos.reportsservice.dto.*;
import com.sanos.reportsservice.service.ContactConversationService;
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
@RequestMapping("/api/reports/contact-conversations")
@CrossOrigin(origins = "*")
@Tag(name = "Chat de contacto", description = "Conversaciones tras aceptar solicitud: mensajes, cierre e historial.")
public class ContactConversationController {

    private final ContactConversationService service;

    public ContactConversationController(ContactConversationService service) {
        this.service = service;
    }

    @Operation(summary = "Listar conversaciones del usuario", description = "status=OPEN|CLOSED|ALL")
    @GetMapping
    public List<ContactConversationDto> list(
            @RequestParam Long userId,
            @RequestParam(required = false, defaultValue = "ALL") String status) {
        return service.listForUser(userId, status);
    }

    @Operation(summary = "Conversacion por solicitud aceptada", description = "Crea el chat si falta (solicitudes aceptadas antes del despliegue).")
    @GetMapping("/by-request/{requestId}")
    public ResponseEntity<?> byRequest(
            @PathVariable Long requestId,
            @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(service.getByRequestId(requestId, userId));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(summary = "Detalle conversacion")
    @GetMapping("/{id}")
    public ResponseEntity<ContactConversationDto> get(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return ResponseEntity.ok(service.getConversation(id, userId));
    }

    @Operation(summary = "Mensajes de la conversacion")
    @GetMapping("/{id}/messages")
    public List<ContactMessageDto> messages(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return service.listMessages(id, userId);
    }

    @Operation(summary = "Enviar mensaje en chat abierto")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = ContactMessageDto.class)))
    @PostMapping("/{id}/messages")
    public ResponseEntity<?> postMessage(
            @PathVariable Long id,
            @RequestBody CreateContactMessageRequest body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.postMessage(id, body));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(summary = "Cerrar chat", description = "Solo el receptor de la solicitud (dueno del reporte).")
    @PatchMapping("/{id}/close")
    public ResponseEntity<?> close(
            @PathVariable Long id,
            @RequestBody CloseContactConversationRequest body) {
        try {
            return ResponseEntity.ok(service.close(id, body));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }
}
