package com.sanos.matchingservice.messaging;

public final class SanosMessaging {

    public static final String EXCHANGE = "sanos.events";
    public static final String ROUTING_REPORT_CREATED = "report.created";
    public static final String QUEUE_MATCHING_REPORT_CREATED = "matching.report.created";

    private SanosMessaging() {
    }
}
