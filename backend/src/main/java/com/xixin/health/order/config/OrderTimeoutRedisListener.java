package com.xixin.health.order.config;

import com.xixin.health.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OrderTimeoutRedisListener implements MessageListener {

    private final OrderService orderService;

    public OrderTimeoutRedisListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String key = new String(message.getBody(), StandardCharsets.UTF_8);
        if (!key.startsWith(OrderService.TIMEOUT_KEY_PREFIX)) {
            return;
        }
        String orderNo = key.substring(OrderService.TIMEOUT_KEY_PREFIX.length());
        boolean cancelled = orderService.cancelExpiredPendingOrder(orderNo);
        log.info("Order timeout redis event handled: orderNo={}, cancelled={}", orderNo, cancelled);
    }
}
