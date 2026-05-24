package com.sanos.auditservice.messaging;

import com.sanos.auditservice.model.LogAuditoria;
import com.sanos.auditservice.repository.LogAuditoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportCreatedEventListenerTest {

    @Mock
    private LogAuditoriaRepository auditRepository;

    @Test
    void onReportCreated_savesAuditEntry() {
        ReportCreatedEventListener listener = new ReportCreatedEventListener(auditRepository);
        ReportCreatedEvent event = new ReportCreatedEvent(
                10L, 20L, 30L, "LOST", "Providencia", "ABIERTO", Instant.now()
        );

        listener.onReportCreated(event);

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(auditRepository).save(captor.capture());
        LogAuditoria saved = captor.getValue();

        assertEquals("REPORTE", saved.getEntidad());
        assertEquals("CREATE_ASYNC", saved.getOperacion());
        assertEquals(30L, saved.getIdUsuarioResponsable());
        assertTrue(saved.getCambiosJson().contains("\"via\":\"RabbitMQ\""));
    }
}
