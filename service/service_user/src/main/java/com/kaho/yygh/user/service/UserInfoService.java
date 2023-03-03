package com.kaho.yygh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaho.yygh.model.user.UserInfo;
import com.kaho.yygh.vo.user.LoginVo;

import java.util.Map;

/**
 * @description:
 * @author: Kaho
 * @create: 2023-03-01 22:19
 **/
public interface UserInfoService extends IService<UserInfo> {

    //用户手机号登录接口
    Map<String, Object> loginUser(LoginVo loginVo);

    //根据openid查询数据库中是否保存有这个微信用户信息
    UserInfo selectWxInfoOpenId(String openid);
}
