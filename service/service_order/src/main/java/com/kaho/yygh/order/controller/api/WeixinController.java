package com.kaho.yygh.order.controller.api;

import com.kaho.yygh.common.result.Result;
import com.kaho.yygh.order.service.PaymentService;
import com.kaho.yygh.order.service.WeixinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @description: 微信支付服务接口 controller 控制类
 * @author: Kaho
 * @create: 2023-03-07 16:55
 **/
@RestController
@RequestMapping("/api/order/weixin")
public class WeixinController {

    @Autowired
    private WeixinService weixinService;

    @Autowired
    private PaymentService paymentService;

    // 根据订单id下单，生成微信支付连接(二维码)
    @GetMapping("createNative/{orderId}")
    public Result createNative(@PathVariable Long orderId) {
        Map map = weixinService.createNative(orderId);
        return Result.ok(map);
    }

    //查询支付状态
    @GetMapping("queryPayStatus/{orderId}")
    public Result queryPayStatus(@PathVariable Long orderId) {
        //调用微信接口实现支付状态查询
        Map<String, String> resultMap = weixinService.queryPayStatus(orderId);
        if(resultMap == null) {
            return Result.fail().message("支付出错");
        }
        //微信第三方返回"支付成功(SUCCESS)"的交易阶段标识
        if("SUCCESS".equals(resultMap.get("trade_state"))) {
            //更新支付状态和订单状态
            String out_trade_no = resultMap.get("out_trade_no"); //订单编码(数据库表中对外业务编码，微信方生成的)
            paymentService.paySuccess(out_trade_no, resultMap);
            return Result.ok().message("支付成功");
        }
        return Result.ok().message("支付中");
    }
}
