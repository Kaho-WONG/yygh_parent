package com.kaho.yygh.msm.service;

import com.kaho.yygh.vo.msm.MsmVo;

/**
 * @description:
 * @author: Kaho
 * @create: 2023-03-02 15:36
 **/
public interface MsmService {

    //发送手机验证码
    boolean send(String phone, String code);

    //rabbitmq使用的发送短信，异步发送预约成功短信给用户
    boolean send(MsmVo msmVo);
}
