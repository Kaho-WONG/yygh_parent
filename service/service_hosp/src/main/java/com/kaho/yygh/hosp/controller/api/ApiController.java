package com.kaho.yygh.hosp.controller.api;

import com.kaho.yygh.common.exception.YyghException;
import com.kaho.yygh.common.helper.HttpRequestHelper;
import com.kaho.yygh.common.result.Result;
import com.kaho.yygh.common.result.ResultCodeEnum;
import com.kaho.yygh.common.util.MD5;
import com.kaho.yygh.hosp.service.HospitalService;
import com.kaho.yygh.hosp.service.HospitalSetService;
import com.kaho.yygh.model.hosp.Hospital;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @description: 平台对外开发的接口都写在该 ApiController 类，外部的医院模拟系统会调用下面编写的接口
 * @author: Kaho
 * @create: 2023-02-24 14:39
 **/
@Api(tags = "医院管理API接口")
@RestController
@RequestMapping("/api/hosp")
public class ApiController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalSetService;

    //上传医院信息接口
    @ApiOperation(value = "上传医院信息")
    @PostMapping("saveHospital")
    /*
     怎么确定这里的形参是request的？从hospital-manager医院接口模拟系统模块的ApiServiceImpl看到,传过来的就是一个request
     JSONObject respone =  HttpRequestHelper.sendRequest(paramMap, this.getApiUrl()+"/api/hosp/saveHospital");
     */
    public Result saveHosp(HttpServletRequest request) {
        //获取传递过来医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        //1 获取医院系统传递过来的签名,签名进行MD5加密
        String hospSign = (String)paramMap.get("sign");

        //2 根据传递过来医院编码，查询数据库，查询签名
        String hoscode = (String)paramMap.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);

        //3 把数据库查询出来签名进行MD5加密
        String signKeyMd5 = MD5.encrypt(signKey);

        //4 判断签名是否一致
        if(!hospSign.equals(signKeyMd5)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        //传输过程中“+”转换为了“ ”，因此我们要转换回来
        String logoData = (String)paramMap.get("logoData");
        logoData = logoData.replaceAll(" ", "+"); //一参数被二参数替换
        paramMap.put("logoData", logoData);

        //调用service的方法将json保存到mongodb
        hospitalService.save(paramMap);
        return Result.ok();
    }

    //查询医院
    @PostMapping("hospital/show")
    public Result getHospital(HttpServletRequest request) {
        //获取传递过来医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //获取医院编号
        String hoscode = (String)paramMap.get("hoscode");
        //1 获取医院系统传递过来的签名,签名进行MD5加密
        String hospSign = (String)paramMap.get("sign");

        //2 根据传递过来医院编码，查询数据库，查询签名
        String signKey = hospitalSetService.getSignKey(hoscode);

        //3 把数据库查询签名进行MD5加密
        String signKeyMd5 = MD5.encrypt(signKey);

        //4 判断签名是否一致
        if(!hospSign.equals(signKeyMd5)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        //调用service方法实现根据医院编号查询
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }
}
