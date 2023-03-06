package com.kaho.yygh.msm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.kaho.yygh.msm.service.MsmService;
import com.kaho.yygh.msm.utils.ConstantPropertiesUtils;
import com.kaho.yygh.vo.msm.MsmVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 封装 阿里云短信服务
 * @author: Kaho
 * @create: 2023-03-02 15:38
 **/
@Service
public class MsmServiceImpl implements MsmService {

    // 封装阿里云短信服务的api，发送手机验证码
    @Override
    public boolean send(String phone, String code) {
        // 判断手机号是否为空，如果为空，短信发送失败
        if(StringUtils.isEmpty(phone)) {
            return false;
        }

        // 整合阿里云短信服务
        // 设置相关参数，这三个参数从配置文件读取出来放进了全局常量中
        DefaultProfile profile = DefaultProfile.
                getProfile(ConstantPropertiesUtils.REGION_Id,
                        ConstantPropertiesUtils.ACCESS_KEY_ID,
                        ConstantPropertiesUtils.SECRECT);
        IAcsClient client = new DefaultAcsClient(profile);

        // 固定配置
        CommonRequest request = new CommonRequest();
        //request.setProtocol(ProtocolType.HTTPS);
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");

        // 发送的目标手机号、签名和模板配置
        // 手机号(这里因为我阿里云使用的是测试短信服务，所以后面手机号只能传入我本人的手机号码)
        request.putQueryParameter("PhoneNumbers", phone);
        // 签名名称(这里拿到我的测试专用签名)
        request.putQueryParameter("SignName", "阿里云短信测试");
        // 模板code(这里拿到我的测试专用模板Code)
        request.putQueryParameter("TemplateCode", "SMS_154950909");
        // 验证码  使用json格式   {"code":"1234"}
        Map<String,Object> param = new HashMap();
        param.put("code", code);
        request.putQueryParameter("TemplateParam", JSONObject.toJSONString(param));

        // 调用方法进行短信发送
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
            return response.getHttpResponse().isSuccess();
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return false;
    }

    //rabbitmq使用的发送短信，异步发送预约成功短信给用户
    @Override
    public boolean send(MsmVo msmVo) {
        if(!StringUtils.isEmpty(msmVo.getPhone())) {
            String code = (String)msmVo.getParam().get("code");
            return this.send(msmVo.getPhone(), code); //这里调用的是上面的重载方法，code就是"预约成功!"
        }
        return false;
    }

    //这段必须保证自己的阿里云短信服务有开通「自定义短信模板」才能使用，这里我没用上
    private boolean send(String phone, Map<String,Object> param) {
        //判断手机号是否为空
        if(StringUtils.isEmpty(phone)) {
            return false;
        }
        //整合阿里云短信服务
        //设置相关参数
        DefaultProfile profile = DefaultProfile.
                getProfile(ConstantPropertiesUtils.REGION_Id,
                        ConstantPropertiesUtils.ACCESS_KEY_ID,
                        ConstantPropertiesUtils.SECRECT);
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        //request.setProtocol(ProtocolType.HTTPS);
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");

        //手机号
        request.putQueryParameter("PhoneNumbers", phone);
        //签名名称
        request.putQueryParameter("SignName", "阿里云短信测试");
        //模板code
        request.putQueryParameter("TemplateCode", "SMS_154950909");

        request.putQueryParameter("TemplateParam", JSONObject.toJSONString(param));

        //调用方法进行短信发送
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
            return response.getHttpResponse().isSuccess();
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return false;
    }

}
