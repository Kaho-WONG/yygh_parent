package com.kaho.yygh.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kaho.yygh.model.user.UserInfo;
import com.kaho.yygh.vo.user.LoginVo;
import com.kaho.yygh.vo.user.UserAuthVo;
import com.kaho.yygh.vo.user.UserInfoQueryVo;

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

    //用户认证
    void userAuth(Long userId, UserAuthVo userAuthVo);

    //用户列表（条件查询带分页）
    IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo);

    //用户锁定(status 0：锁定 1：正常)
    void lock(Long userId, Integer status);

    //用户详情
    Map<String, Object> show(Long userId);

    //认证审批(authStatus 2：通过 -1：不通过)
    void approval(Long userId, Integer authStatus);
}
