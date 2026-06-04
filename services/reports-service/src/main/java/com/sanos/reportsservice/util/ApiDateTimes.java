package com.sanos.reportsservice.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/** Serializa LocalDateTime (reloj UTC del servidor) como instante ISO-8601 con Z. */
public final class ApiDateTimes {

    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;

    private ApiDateTimes() {}

    public static String format(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return ISO_INSTANT.format(value.atZone(ZoneOffset.UTC).toInstant());
    }
}
