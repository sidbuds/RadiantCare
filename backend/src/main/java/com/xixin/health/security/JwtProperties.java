package com.xixin.health.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.security")
public class JwtProperties {
    /** JWT密钥 */
    private String jwtSecret;
    /** 过期时间(秒) */
    private Long expireSeconds;
}
