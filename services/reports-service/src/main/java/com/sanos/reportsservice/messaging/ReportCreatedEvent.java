package com.sanos.reportsservice.messaging;

import java.time.Instant;

/**
 * Evento publicado cuando se crea un reporte (cola asíncrona).
 */
public record ReportCreatedEvent(
        Long reportId,
        Long petId,
        Long createdByUserId,
        String reportType,
        String commune,
        String status,
        Instant occurredAt
) {
}
