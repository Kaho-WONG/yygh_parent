package com.kaho.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaho.yygh.model.order.OrderInfo;
import com.kaho.yygh.model.order.PaymentInfo;

import java.util.Map;

/**
 * @description: 支付表 payment_info 操作
 * @author: Kaho
 * @create: 2023-03-07 17:32
 **/
public interface PaymentService extends IService<PaymentInfo> {

    //向支付记录表添加信息
    void savePaymentInfo(OrderInfo order, Integer status);

    //支付成功  更新支付状态和订单状态
    void paySuccess(String out_trade_no, Map<String, String> resultMap);

    //获取支付记录
    PaymentInfo getPaymentInfo(Long orderId, Integer paymentType);
}
