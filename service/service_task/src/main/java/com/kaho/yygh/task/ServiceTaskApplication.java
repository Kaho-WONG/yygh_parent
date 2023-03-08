package com.kaho.yygh.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @description: 定时任务模块启动类 就医提醒
 * @author: Kaho
 * @create: 2023-03-08 16:17
 **/
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class) //不使用数据库，取消数据源自动配置
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.kaho"})
public class ServiceTaskApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceTaskApplication.class, args);
    }
}

