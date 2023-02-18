package com.kaho.yygh.hosp.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description: 医院管理模块配置类
 * @author: Kaho
 * @create: 2023-02-14 22:37
 **/
@Configuration
@MapperScan("com.kaho.yygh.hosp.mapper") // mapper扫描
public class HospConfig {
    /**
     * 分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }
}
