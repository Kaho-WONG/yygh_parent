package com.kaho.yygh.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @description: 预约挂号订单模块
 * @author: Kaho
 * @create: 2023-03-05 22:30
 **/
@SpringBootApplication
@ComponentScan(basePackages = {"com.kaho"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.kaho"})
public class ServiceOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceOrderApplication.class, args);
    }
}

