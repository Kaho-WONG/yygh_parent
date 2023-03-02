package com.kaho.yygh.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @description: 用户模块
 * @author: Kaho
 * @create: 2023-03-01 21:38
 **/
@SpringBootApplication
@ComponentScan(basePackages = "com.kaho")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.kaho")
public class ServiceUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceUserApplication.class, args);
    }
}

