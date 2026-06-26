package com.xixin.health.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 业务单号生成器 - 格式: 前缀 + 时间戳 + 4位序列号
 */
public final class NoGenerator {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private NoGenerator() {
    }

    /** 生成下一个单号 */
    public static String next(String prefix) {
        int sequence = COUNTER.getAndIncrement() % 10000;
        return prefix + LocalDateTime.now().format(FORMATTER) + String.format("%04d", sequence);
    }
}
