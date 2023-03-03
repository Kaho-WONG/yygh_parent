package com.kaho.yygh.user.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @description:该配置类用于启动服务时第一时间将 application.properties 中的微信登录相关配置读取。
 * InitializingBean是Spring提供的拓展性接口，InitializingBean接口为bean提供了属性初始化后的处理方法，
 * 它只有一个afterPropertiesSet方法，凡是继承该接口的类，在bean的属性初始化后都会执行该方法。
 * @author: Kaho
 * @create: 2023-03-03 14:50
 **/
@Component
public class ConstantWxPropertiesUtils implements InitializingBean {

    @Value("${wx.open.app_id}")
    private String appId;

    @Value("${wx.open.app_secret}")
    private String appSecret;

    @Value("${wx.open.redirect_url}")
    private String redirectUrl;

    @Value("${yygh.baseUrl}")
    private String yyghBaseUrl;

    public static String WX_OPEN_APP_ID;
    public static String WX_OPEN_APP_SECRET;
    public static String WX_OPEN_REDIRECT_URL;

    public static String YYGH_BASE_URL;

    @Override
    public void afterPropertiesSet() throws Exception {
        WX_OPEN_APP_ID = appId;
        WX_OPEN_APP_SECRET = appSecret;
        WX_OPEN_REDIRECT_URL = redirectUrl;
        YYGH_BASE_URL = yyghBaseUrl;
    }
}

