package com.xixin.health.order.scheduler;

import com.xixin.health.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderTimeoutScheduler {

    private final OrderService orderService;

    public OrderTimeoutScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedDelay = 60000)
    public void cancelExpiredPendingOrders() {
        int count = orderService.cancelExpiredPendingOrders();
        if (count > 0) {
            log.info("Order timeout compensation done, cancelled {} orders", count);
        }
    }
}
