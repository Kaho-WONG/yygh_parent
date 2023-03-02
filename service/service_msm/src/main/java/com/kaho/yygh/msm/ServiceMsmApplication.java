package com.kaho.yygh.msm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @description:
 * @author: Kaho
 * @create: 2023-03-02 15:17
 **/
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class) //取消数据源自动配置，不需要加载(mysql)数据库配置
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.kaho"})
public class ServiceMsmApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceMsmApplication.class, args);
    }
}