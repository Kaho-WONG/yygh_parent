package com.kaho.yygh.order.client;

import com.kaho.yygh.vo.order.OrderCountQueryVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @description: 订单模块 Feign 服务调用接口，供统计服务模块调用
 * @author: Kaho
 * @create: 2023-03-08 20:22
 **/
@FeignClient(value = "service-order")
@Component
public interface OrderFeignClient {
    /**
     * 根据排班id获取预约下单数据
     */
    @PostMapping("/api/order/orderInfo/inner/getCountMap")
    Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo);
}
