package com.kaho.yygh.user.controller.api;

import com.kaho.yygh.common.result.Result;
import com.kaho.yygh.user.service.UserInfoService;
import com.kaho.yygh.vo.user.LoginVo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
