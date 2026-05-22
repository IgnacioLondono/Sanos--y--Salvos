package com.sanos.reportsservice.messaging;

import com.sanos.reportsservice.model.ReporteEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ReportEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ReportEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final boolean enabled;

    public ReportEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${sanos.messaging.enabled:true}") boolean enabled) {
        this.rabbitTemplate = rabbitTemplate;
        this.enabled = enabled;
    }

    public void publishReportCreated(ReporteEvento report) {
        if (!enabled || report == null || report.getIdReporte() == null) {
            return;
        }
        ReportCreatedEvent event = new ReportCreatedEvent(
                report.getIdReporte(),
                report.getIdMascota(),
                report.getIdUsuarioCreador(),
                report.getTipoReporte(),
                report.getComuna(),
                report.getEstado(),
                report.getFechaCreacion() != null
                        ? report.getFechaCreacion().atZone(java.time.ZoneId.systemDefault()).toInstant()
                        : Instant.now()
        );
        try {
            rabbitTemplate.convertAndSend(
                    SanosMessaging.EXCHANGE,
                    SanosMessaging.ROUTING_REPORT_CREATED,
                    event
            );
            log.info("Evento RabbitMQ publicado: report.created id={}", report.getIdReporte());
        } catch (Exception ex) {
            log.warn("No se pudo publicar en RabbitMQ (el reporte ya fue guardado): {}", ex.getMessage());
        }
    }
}
