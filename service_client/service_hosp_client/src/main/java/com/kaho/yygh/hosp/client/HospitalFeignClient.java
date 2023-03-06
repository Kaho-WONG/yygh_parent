package com.kaho.yygh.hosp.client;

import com.kaho.yygh.vo.hosp.ScheduleOrderVo;
import com.kaho.yygh.vo.order.SignInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @description: 医院模块服务远程调用 Feign 接口类
 * @author: Kaho
 * @create: 2023-03-06 12:56
 **/
@FeignClient(value = "service-hosp")
@Component
public interface HospitalFeignClient {

    //根据排班id获取预约下单信息(医院科室排班医生信息、剩余预约数、费用、时间等)
    @GetMapping("/api/hosp/hospital/inner/getScheduleOrderVo/{scheduleId}")
    ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId);

    //获取医院签名信息(api基础路径、签名秘钥)
    @GetMapping("/api/hosp/hospital/inner/getSignInfoVo/{hoscode}")
    SignInfoVo getSignInfoVo(@PathVariable("hoscode") String hoscode);

}
