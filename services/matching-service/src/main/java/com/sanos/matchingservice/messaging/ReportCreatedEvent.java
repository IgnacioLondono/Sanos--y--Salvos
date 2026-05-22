package com.sanos.matchingservice.messaging;

import java.time.Instant;

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
