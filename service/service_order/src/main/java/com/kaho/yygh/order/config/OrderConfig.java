package com.kaho.yygh.order.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @description: 配置类，作用就是拿来扫描mapper，就不需要手动在每个mapper上加@Mapper注解
 * @author: Kaho
 * @create: 2023-03-05 23:30
 **/
@Configuration
@MapperScan("com.kaho.yygh.order.mapper")
public class OrderConfig {
}
