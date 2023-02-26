package com.kaho.yygh.hosp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @description: 医院模块主启动类
 * @author: Kaho
 * @create: 2023-02-14 20:54
 **/
@SpringBootApplication
@ComponentScan("com.kaho") // 扫描到 common - service_util 下的 Swagger 配置
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.kaho")
public class ServiceHospApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceHospApplication.class, args);
    }
}
