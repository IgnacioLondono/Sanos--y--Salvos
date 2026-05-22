package com.sanos.matchingservice.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor asíncrono: al llegar un reporte nuevo se puede lanzar matching sin bloquear al ciudadano.
 */
@Component
public class ReportCreatedEventListener {

    private static final Logger log = LoggerFactory.getLogger(ReportCreatedEventListener.class);

    @RabbitListener(queues = SanosMessaging.QUEUE_MATCHING_REPORT_CREATED)
    public void onReportCreated(ReportCreatedEvent event) {
        log.info(
                "RabbitMQ matching: nuevo reporte id={} tipo={} comuna={} — listo para motor de coincidencias",
                event.reportId(),
                event.reportType(),
                event.commune()
        );
    }
}
