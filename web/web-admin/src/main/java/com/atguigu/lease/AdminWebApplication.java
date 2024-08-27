package com.atguigu.lease;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

//springboot启动类
@EnableScheduling       //启用Spring Boot定时任务
@SpringBootApplication(scanBasePackages = "com.atguigu.lease")
@MapperScan("com.atguigu.lease.web.*.mapper")
public class AdminWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminWebApplication.class, args);
    }
}