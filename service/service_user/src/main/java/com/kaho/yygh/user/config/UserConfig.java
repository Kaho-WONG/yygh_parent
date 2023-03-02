package com.kaho.yygh.user.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @description: 用户模块配置类
 * @author: Kaho
 * @create: 2023-03-01 22:27
 **/
@Configuration
@MapperScan("com.kaho.yygh.user.mapper")
public class UserConfig {
}
