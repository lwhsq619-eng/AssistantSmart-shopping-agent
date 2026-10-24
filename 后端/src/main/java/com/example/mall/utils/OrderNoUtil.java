package com.example.mall.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public final class OrderNoUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private OrderNoUtil() {
    }

    public static String next() {
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "SO" + LocalDateTime.now().format(FORMATTER) + random;
    }
}
