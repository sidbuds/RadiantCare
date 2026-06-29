package com.xixin.health.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisKeyspaceNotificationInitializer implements ApplicationRunner {

    private final RedisConnectionFactory connectionFactory;

    public RedisKeyspaceNotificationInitializer(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void run(ApplicationArguments args) {
        RedisConnection connection = null;
        try {
            connection = connectionFactory.getConnection();
            connection.setConfig("notify-keyspace-events", "Ex");
        } catch (Exception ex) {
            log.warn("Enable Redis keyspace notification failed, order timeout scheduler will still compensate", ex);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
