package com.xixin.health;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xixin.health.**.mapper")
public class XixinHealthApplication {

    public static void main(String[] args) {
        SpringApplication.run(XixinHealthApplication.class, args);
    }
}
