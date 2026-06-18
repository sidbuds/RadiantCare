package com.xixin.health.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.security")
public class JwtProperties {
    private String jwtSecret;
    private Long expireSeconds;
}
