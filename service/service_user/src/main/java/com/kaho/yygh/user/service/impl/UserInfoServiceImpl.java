package com.kaho.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaho.yygh.common.exception.YyghException;
import com.kaho.yygh.common.helper.JwtHelper;
import com.kaho.yygh.common.result.ResultCodeEnum;
import com.kaho.yygh.enums.AuthStatusEnum;
import com.kaho.yygh.model.user.UserInfo;
import com.kaho.yygh.user.mapper.UserInfoMapper;
import com.kaho.yygh.user.service.UserInfoService;
import com.kaho.yygh.vo.user.LoginVo;
import com.kaho.yygh.vo.user.UserAuthVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: Kaho
 * @create: 2023-03-01 22:19
 **/
@Service
public class UserInfoServiceImpl extends
        ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    //用户手机号登录接口
    @Override
    public Map<String, Object> loginUser(LoginVo loginVo) {
        //从loginVo获取输入的手机号，和验证码
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        //判断手机号和验证码是否为空
        if(StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        //判断Redis中存储的手机验证码和用户输入的验证码是否一致
        String redisCode = redisTemplate.opsForValue().get(phone);
        if(!code.equals(redisCode)) {
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }

        //如果是微信扫码登录，则Openid有值，则需要绑定手机号码，执行后userInfo就!=null了不会走61行的手机号登录
        UserInfo userInfo = null;
        if(!StringUtils.isEmpty(loginVo.getOpenid())) {
            userInfo = this.selectWxInfoOpenId(loginVo.getOpenid());
            if(null != userInfo) {
                userInfo.setPhone(loginVo.getPhone()); //绑定手机号
                this.updateById(userInfo);
            } else {
                throw new YyghException(ResultCodeEnum.DATA_ERROR);
            }
        }
        //如果userinfo为空，证明没用微信登录过且本次也不是通过微信登录，进行正常手机登录
        if(userInfo == null) {
            //判断是否第一次登录：根据手机号查询数据库，如果不存在相同手机号就是第一次登录
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("phone",phone);
            userInfo = baseMapper.selectOne(wrapper);
            //如果userInfo不为null，则不执行if里面
            if(userInfo == null) { //第一次使用这个手机号登录
                //添加信息到数据库
                userInfo = new UserInfo();
                userInfo.setName("");
                userInfo.setPhone(phone);
                userInfo.setStatus(1);
                baseMapper.insert(userInfo);
            }
        }

        //校验是否被禁用，如若用户被禁用，则直接抛出异常
        if(userInfo.getStatus() == 0) {
            throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }

        //当前用户不是第一次登录，且未被禁用，则直接登录。
        //返回登录信息(登录用户名)
        //返回token信息，token信息是用来返回给前台的，执行操作时判断用户是否登录状态，可以设置过期时间(用session一样)
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        //如果这个用户登录后没有去设置真实姓名，则name为空，那我们就设置该用户它在前端显示的名字为昵称
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        //如果这个用户登录后也没有设置昵称，则name还是空，那我们就设置该用户它在前端显示的名字为它的手机号
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);

        //jwt生成token字符串
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token",token);

        return map;
    }

    //根据openid查询数据库中是否保存了这个微信信息
    @Override
    public UserInfo selectWxInfoOpenId(String openid) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid", openid);
        UserInfo userInfo = baseMapper.selectOne(queryWrapper);
        return userInfo;
    }

    //用户认证
    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //根据用户id查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);
        //设置认证信息
        //认证人姓名
        userInfo.setName(userAuthVo.getName());
        //其他认证信息
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus()); //设置为认证中状态，等待管理员在管理平台审核
        //进行信息更新
        baseMapper.updateById(userInfo);
    }
}
