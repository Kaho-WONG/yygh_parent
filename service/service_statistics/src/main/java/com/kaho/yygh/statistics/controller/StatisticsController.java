package com.kaho.yygh.statistics.controller;

import com.kaho.yygh.common.result.Result;
import com.kaho.yygh.order.client.OrderFeignClient;
import com.kaho.yygh.vo.order.OrderCountQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @description: 统计服务 api 接口，供管理平台前端调用
 * @author: Kaho
 * @create: 2023-03-08 20:32
 **/
@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    //获取预约统计数据
    @GetMapping("getCountMap")
    public Result getCountMap(OrderCountQueryVo orderCountQueryVo) {
        Map<String, Object> countMap = orderFeignClient.getCountMap(orderCountQueryVo);
        return Result.ok(countMap);
    }
}
