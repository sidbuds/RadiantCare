package com.xixin.health.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 跨域配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {
    /** 允许的来源域名列表 */
    private List<String> allowedOrigins = new ArrayList<String>();
}
