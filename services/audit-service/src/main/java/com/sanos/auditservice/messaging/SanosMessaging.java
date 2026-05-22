package com.sanos.auditservice.messaging;

public final class SanosMessaging {

    public static final String EXCHANGE = "sanos.events";
    public static final String ROUTING_REPORT_CREATED = "report.created";
    public static final String QUEUE_AUDIT_REPORT_CREATED = "audit.report.created";

    private SanosMessaging() {
    }
}
