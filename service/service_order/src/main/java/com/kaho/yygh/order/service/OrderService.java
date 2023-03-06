package com.kaho.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaho.yygh.model.order.OrderInfo;

/**
 * @description:
 * @author: Kaho
 * @create: 2023-03-05 22:36
 **/
public interface OrderService extends IService<OrderInfo> {

    //保存生成挂号订单
    Long saveOrder(String scheduleId, Long patientId);
}
