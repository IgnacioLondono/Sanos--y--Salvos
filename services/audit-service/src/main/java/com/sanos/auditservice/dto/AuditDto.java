package com.sanos.auditservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuditDto", description = "Linea de auditoria (tabla log_auditoria, db_audit).")
public record AuditDto(
        @Schema(description = "PK id_log", accessMode = Schema.AccessMode.READ_ONLY) Long id,
        @Schema(description = "Entidad o tabla logica afectada", example = "Mascota") String entity,
        @Schema(description = "Operacion CREATE/UPDATE/DELETE", example = "CREATE") String operation,
        @Schema(description = "Actor (usuario o sistema)", example = "admin@sanosysalvos.cl") String actor,
        @Schema(description = "JSON cambios o payload resumido") String changes,
        @Schema(description = "creado_en ISO", accessMode = Schema.AccessMode.READ_ONLY) String createdAt
) {}
