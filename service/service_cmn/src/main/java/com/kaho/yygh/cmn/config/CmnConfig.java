package com.kaho.yygh.cmn.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description: 公共服务模块配置类
 * @author: Kaho
 * @create: 2023-02-18 14:37
 **/
@Configuration
@MapperScan("com.kaho.yygh.cmn.mapper") // mapper扫描
public class CmnConfig {
    /**
     * 分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }
}
