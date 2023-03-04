package com.kaho.yygh.user.controller.api;

import com.kaho.yygh.common.result.Result;
import com.kaho.yygh.common.utils.AuthContextHolder;
import com.kaho.yygh.model.user.UserInfo;
import com.kaho.yygh.user.service.UserInfoService;
import com.kaho.yygh.vo.user.LoginVo;
import com.kaho.yygh.vo.user.UserAuthVo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @description: 用户模块 用户前台接口调用api控制类
 * @author: Kaho
 * @create: 2023-03-01 22:04
 **/
@Api
@RestController
@RequestMapping("/api/user")
public class UserInfoApiController {

    @Autowired
    private UserInfoService userInfoService;

    //用户手机号登录接口
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo) {
        Map<String,Object> info = userInfoService.loginUser(loginVo);
        return Result.ok(info);
    }

    //用户认证接口
    @PostMapping("auth/userAuth")
    public Result userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request) {
        //其实就是往数据库中对应用户id的user_info中补充一些认证数据进去，根据id查出记录然后修改记录
        //传递两个参数，第一个参数用户id，第二个参数认证数据vo对象
        userInfoService.userAuth(AuthContextHolder.getUserId(request),userAuthVo);
        return Result.ok();
    }

    //获取用户id信息接口
    @GetMapping("auth/getUserInfo")
    public Result getUserInfo(HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId(request);
        UserInfo userInfo = userInfoService.getById(userId); //这里直接用mybatisplus提供的api
        return Result.ok(userInfo);
    }
}
