package com.kaho.yygh.user.client;

import com.kaho.yygh.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @description: 用户模块服务远程调用 Feign 接口类
 * @author: Kaho
 * @create: 2023-03-06 12:35
 **/
@FeignClient(value = "service-user")
@Component
public interface PatientFeignClient {

    //获取就诊人
    @GetMapping("/api/user/patient/inner/get/{id}")
    Patient getPatientOrder(@PathVariable("id") Long id);

}

