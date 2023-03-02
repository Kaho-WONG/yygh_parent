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
}
