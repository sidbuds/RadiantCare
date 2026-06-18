package com.xixin.health.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public final class NoGenerator {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private NoGenerator() {
    }

    public static String next(String prefix) {
        int sequence = COUNTER.getAndIncrement() % 10000;
        return prefix + LocalDateTime.now().format(FORMATTER) + String.format("%04d", sequence);
    }
}
