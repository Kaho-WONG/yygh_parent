package com.kaho.yygh.msm.service;

/**
 * @description:
 * @author: Kaho
 * @create: 2023-03-02 15:36
 **/
public interface MsmService {

    //发送手机验证码
    boolean send(String phone, String code);

}
