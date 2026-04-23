package com.sanos.bff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/bff")
@CrossOrigin(origins = "*")
@Tag(name = "BFF", description = "Agregacion para frontend: llama a IAM, pets, reports, geo, media, matching, capacity, audit. Sin tablas propias.")
public class BffController {

    private final RestTemplate restTemplate;

    @Value("${services.iamUrl}")        private String iamUrl;
    @Value("${services.petCatalogUrl}") private String petCatalogUrl;
    @Value("${services.mediaUrl}")      private String mediaUrl;
    @Value("${services.reportsUrl}")    private String reportsUrl;
    @Value("${services.capacityUrl}")   private String capacityUrl;
    @Value("${services.matchingUrl}")   private String matchingUrl;
    @Value("${services.zonesUrl}")      private String zonesUrl;
    @Value("${services.auditUrl}")      private String auditUrl;

    public BffController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Operation(
            summary = "Dashboard consolidado",
            description = """
                    Agrega listados y totales: mascotas, reportes, capacity, media, matching, zonas, auditoria, usuarios IAM.
                    Incluye `serviceStatus` por microservicio (UP/DOWN) si alguna llamada falla.
                    Respuesta: objeto JSON con claves totalPets, totalReports, pets, reports, … (tipo object).""")
    @ApiResponse(responseCode = "200", description = "Mapa agregado",
            content = @Content(mediaType = "application/json", schema = @Schema(type = "object")))
    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        Map<String, Object> serviceStatus = new LinkedHashMap<>();

        List<?> pets      = safeList("pet-catalog-service",    serviceStatus, () -> restTemplate.getForObject(petCatalogUrl + "/api/pets",     List.class));
        List<?> reports   = safeList("reports-service",        serviceStatus, () -> restTemplate.getForObject(reportsUrl    + "/api/reports",  List.class));
        List<?> capacity  = safeList("capacity-service",       serviceStatus, () -> restTemplate.getForObject(capacityUrl   + "/api/capacity", List.class));
        List<?> media     = safeList("media-service",          serviceStatus, () -> restTemplate.getForObject(mediaUrl      + "/api/media",    List.class));
        List<?> matching  = safeList("matching-service",       serviceStatus, () -> restTemplate.getForObject(matchingUrl   + "/api/matching", List.class));
        List<?> zones     = safeList("geo-intelligence-service", serviceStatus, () -> restTemplate.getForObject(zonesUrl    + "/api/zones",    List.class));
        List<?> audit     = safeList("audit-service",          serviceStatus, () -> restTemplate.getForObject(auditUrl      + "/api/audit",    List.class));
        List<?> users     = safeList("iam-service",            serviceStatus, () -> restTemplate.getForObject(iamUrl        + "/api/iam/users", List.class));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalPets",             sizeOf(pets));
        result.put("totalReports",          sizeOf(reports));
        result.put("totalCapacityRecords",  sizeOf(capacity));
        result.put("totalPhotos",           sizeOf(media));
        result.put("totalMatchingRecords",  sizeOf(matching));
        result.put("totalZones",            sizeOf(zones));
        result.put("totalAuditEvents",      sizeOf(audit));
        result.put("totalUsers",            sizeOf(users));
        result.put("pets",       pets);
        result.put("reports",    reports);
        result.put("capacity",   capacity);
        result.put("media",      media);
        result.put("matching",   matching);
        result.put("zones",      zones);
        result.put("audit",      audit);
        result.put("serviceStatus", serviceStatus);
        return result;
    }

    @Operation(
            summary = "Datos para mapa",
            description = "Combina `GET /api/reports` y `GET /api/zones` para capas mapa (Leaflet). Incluye `serviceStatus`.")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(type = "object")))
    @GetMapping("/map")
    public Map<String, Object> mapOverview() {
        Map<String, Object> serviceStatus = new LinkedHashMap<>();
        List<?> reports = safeList("reports-service",           serviceStatus, () -> restTemplate.getForObject(reportsUrl + "/api/reports", List.class));
        List<?> zones   = safeList("geo-intelligence-service",  serviceStatus, () -> restTemplate.getForObject(zonesUrl   + "/api/zones",   List.class));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reports", reports);
        result.put("zones",   zones);
        result.put("serviceStatus", serviceStatus);
        return result;
    }

    @Operation(
            summary = "Vista mascota enriquecida",
            description = "Unifica mascota (`/api/pets/{id}`), reportes por mascota y media por mascota.")
    @ApiResponse(responseCode = "200", description = "pet, reports, media, serviceStatus (pet null si no existe mascota).",
            content = @Content(mediaType = "application/json", schema = @Schema(type = "object")))
    @GetMapping("/pet-overview/{petId}")
    public ResponseEntity<Map<String, Object>> petOverview(
            @Parameter(description = "id_mascota (PK catalogo)", example = "1", required = true) @PathVariable Long petId) {
        Map<String, Object> serviceStatus = new LinkedHashMap<>();

        Object pet = safeObject("pet-catalog-service", serviceStatus,
                () -> restTemplate.getForObject(petCatalogUrl + "/api/pets/" + petId, Object.class));
        List<?> reports = safeList("reports-service", serviceStatus,
                () -> restTemplate.getForObject(reportsUrl + "/api/reports/pet/" + petId, List.class));
        List<?> media = safeList("media-service", serviceStatus,
                () -> restTemplate.getForObject(mediaUrl + "/api/media/pet/" + petId, List.class));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pet",           pet);
        result.put("reports",       reports);
        result.put("media",         media);
        result.put("serviceStatus", serviceStatus);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Salud BFF")
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "component", "bff");
    }

    private Integer sizeOf(List<?> list) {
        return list == null ? 0 : list.size();
    }

    private List<?> safeList(String serviceName, Map<String, Object> serviceStatus, Supplier<List<?>> supplier) {
        try {
            List<?> data = supplier.get();
            serviceStatus.put(serviceName, Map.of("status", "UP"));
            return data == null ? List.of() : data;
        } catch (Exception ex) {
            serviceStatus.put(serviceName, Map.of("status", "DOWN", "error", sanitizeError(ex.getMessage())));
            return List.of();
        }
    }

    private Object safeObject(String serviceName, Map<String, Object> serviceStatus, Supplier<Object> supplier) {
        try {
            Object data = supplier.get();
            serviceStatus.put(serviceName, Map.of("status", "UP"));
            return data;
        } catch (Exception ex) {
            serviceStatus.put(serviceName, Map.of("status", "DOWN", "error", sanitizeError(ex.getMessage())));
            return null;
        }
    }

    private String sanitizeError(String message) {
        if (message == null || message.isBlank()) return "unavailable";
        return message.length() > 120 ? message.substring(0, 120) + "..." : message;
    }
}
