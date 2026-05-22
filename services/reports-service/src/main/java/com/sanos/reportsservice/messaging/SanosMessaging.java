package com.sanos.reportsservice.messaging;

/**
 * Contrato de mensajería asíncrona (RabbitMQ) compartido entre microservicios.
 */
public final class SanosMessaging {

    public static final String EXCHANGE = "sanos.events";
    public static final String ROUTING_REPORT_CREATED = "report.created";

    private SanosMessaging() {
    }
}
