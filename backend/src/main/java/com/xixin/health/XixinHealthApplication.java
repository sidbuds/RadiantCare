package com.xixin.health;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 熙心健康体检平台启动类
 */
@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
@MapperScan("com.xixin.health.**.mapper")
public class XixinHealthApplication {

    public static void main(String[] args) {
        SpringApplication.run(XixinHealthApplication.class, args);
    }
}
