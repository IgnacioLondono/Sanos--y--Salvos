package com.sanos.auditservice.messaging;

import com.sanos.auditservice.model.LogAuditoria;
import com.sanos.auditservice.repository.LogAuditoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class ReportCreatedEventListener {

    private static final Logger log = LoggerFactory.getLogger(ReportCreatedEventListener.class);

    private final LogAuditoriaRepository auditRepository;

    public ReportCreatedEventListener(LogAuditoriaRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @RabbitListener(queues = SanosMessaging.QUEUE_AUDIT_REPORT_CREATED)
    @Transactional
    public void onReportCreated(ReportCreatedEvent event) {
        log.info("RabbitMQ: reporte creado recibido id={}", event.reportId());

        LogAuditoria entry = new LogAuditoria();
        entry.setEntidad("REPORTE");
        entry.setOperacion("CREATE_ASYNC");
        entry.setActor("rabbitmq-audit-consumer");
        entry.setIdUsuarioResponsable(event.createdByUserId());
        entry.setTablaAfectada("reportes_eventos");
        entry.setAccionRealizada("CREATE_ASYNC");
        entry.setCambiosJson(String.format(
                "{\"reportId\":%d,\"petId\":%s,\"type\":\"%s\",\"commune\":\"%s\",\"via\":\"RabbitMQ\"}",
                event.reportId(),
                event.petId(),
                event.reportType(),
                event.commune()
        ));
        entry.setCreadoEn(LocalDateTime.now());
        auditRepository.save(entry);
    }
}
