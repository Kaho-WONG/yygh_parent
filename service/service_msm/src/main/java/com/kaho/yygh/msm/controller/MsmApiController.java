package com.kaho.yygh.msm.controller;

import com.kaho.yygh.common.result.Result;
import com.kaho.yygh.msm.service.MsmService;
import com.kaho.yygh.msm.utils.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @description: 对外提供发送手机验证码短信服务的 api 接口
 * @author: Kaho
 * @create: 2023-03-02 15:35
 **/
@RestController
@RequestMapping("/api/msm")
public class MsmApiController {

    @Autowired
    private MsmService msmService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 发送手机验证码短信
    @GetMapping("send/{phone}")
    public Result sendCode(@PathVariable String phone) {
        // 从 redis 获取验证码，如果获取到，证明阿里云在过期时间内发送过一次验证码到手机上了，可以直接用这个验证码，返回ok
        String code = redisTemplate.opsForValue().get(phone); // key 手机号 — value 验证码
        if(!StringUtils.isEmpty(code)) {
            return Result.ok();
        }

        // 如果从 redis 获取不到，证明未给这个手机发送过验证码或验证码已经过期，需要重新发送新的验证码
        code = RandomUtil.getFourBitRandom(); // 生成四位验证码
        // 调用 service 方法，通过整合短信服务进行发送
        boolean isSend = msmService.send(phone, code);
        // 生成验证码放到redis里面，设置有效时间（2分钟）
        if(isSend) { // 发送成功
            // key - value - 过期时间 - 时间单位
            redisTemplate.opsForValue().set(phone, code, 2, TimeUnit.MINUTES);
            return Result.ok();
        } else { // 发送失败
            return Result.fail().message("发送短信失败");
        }
    }
}
