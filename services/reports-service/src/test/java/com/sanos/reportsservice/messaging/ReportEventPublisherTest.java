package com.sanos.reportsservice.messaging;

import com.sanos.reportsservice.model.ReporteEvento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void publishReportCreated_sendsMessageWhenEnabled() {
        ReportEventPublisher publisher = new ReportEventPublisher(rabbitTemplate, true);
        ReporteEvento report = baseReport();

        publisher.publishReportCreated(report);

        verify(rabbitTemplate).convertAndSend(
                eq(SanosMessaging.EXCHANGE),
                eq(SanosMessaging.ROUTING_REPORT_CREATED),
                any(ReportCreatedEvent.class)
        );
    }

    @Test
    void publishReportCreated_skipsWhenDisabled() {
        ReportEventPublisher publisher = new ReportEventPublisher(rabbitTemplate, false);

        publisher.publishReportCreated(baseReport());

        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void publishReportCreated_skipsWhenMissingId() {
        ReportEventPublisher publisher = new ReportEventPublisher(rabbitTemplate, true);
        ReporteEvento report = baseReport();
        report.setIdReporte(null);

        publisher.publishReportCreated(report);

        verifyNoInteractions(rabbitTemplate);
    }

    private ReporteEvento baseReport() {
        ReporteEvento report = new ReporteEvento();
        report.setIdReporte(77L);
        report.setIdMascota(11L);
        report.setIdUsuarioCreador(22L);
        report.setTipoReporte("LOST");
        report.setComuna("Santiago");
        report.setEstado("ABIERTO");
        report.setLatitud(new BigDecimal("-33.4"));
        report.setLongitud(new BigDecimal("-70.6"));
        report.setFechaCreacion(LocalDateTime.now());
        return report;
    }
}
