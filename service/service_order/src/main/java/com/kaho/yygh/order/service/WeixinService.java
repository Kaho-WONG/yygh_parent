package com.kaho.yygh.order.service;

import java.util.Map;

/**
 * @description:
 * @author: Kaho
 * @create: 2023-03-07 16:57
 **/
public interface WeixinService {

    // 根据订单号下单，生成微信支付连接(二维码)
    Map createNative(Long orderId);

    // 根据订单id去微信第三方查询支付状态
    Map<String, String> queryPayStatus(Long orderId);

    // 微信退款
    Boolean refund(Long orderId);
}
