package com.kaho.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaho.yygh.common.exception.YyghException;
import com.kaho.yygh.common.helper.HttpRequestHelper;
import com.kaho.yygh.common.result.ResultCodeEnum;
import com.kaho.yygh.enums.OrderStatusEnum;
import com.kaho.yygh.enums.PaymentStatusEnum;
import com.kaho.yygh.enums.PaymentTypeEnum;
import com.kaho.yygh.hosp.client.HospitalFeignClient;
import com.kaho.yygh.model.order.OrderInfo;
import com.kaho.yygh.model.order.PaymentInfo;
import com.kaho.yygh.order.mapper.PaymentMapper;
import com.kaho.yygh.order.service.OrderService;
import com.kaho.yygh.order.service.PaymentService;
import com.kaho.yygh.vo.order.SignInfoVo;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 支付表 payment_info 操作
 * @author: Kaho
 * @create: 2023-03-07 17:38
 **/
@Service
public class PayServiceImpl extends
        ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    //向支付记录表添加信息(paymentType: 支付类型 1：微信 2：支付宝)
    @Override
    public void savePaymentInfo(OrderInfo order, Integer paymentType) {
        //根据订单id和支付类型，查询支付记录表是否存在相同订单
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", order.getId()); // 订单id
        wrapper.eq("payment_type", paymentType); // 支付类型
        Integer count = baseMapper.selectCount(wrapper);
        if(count > 0) {
            return;
        }
        //添加记录进支付记录表 payment_info
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setOutTradeNo(order.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus()); //状态: 支付中
        String subject = new DateTime(order.getReserveDate()).toString("yyyy-MM-dd")
                + "|" + order.getHosname() + "|" + order.getDepname() + "|" + order.getTitle();
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(order.getAmount());
        baseMapper.insert(paymentInfo);
    }

    //支付成功  更新支付状态和订单状态
    @Override
    public void paySuccess(String out_trade_no, Map<String, String> resultMap) {
        //1 根据订单编号(微信支付对外业务编号)得到支付记录
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no", out_trade_no);
        wrapper.eq("payment_type", PaymentTypeEnum.WEIXIN.getStatus());
        PaymentInfo paymentInfo = baseMapper.selectOne(wrapper);

        //2 更新支付记录信息(yygh_order库-payment_info表)
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus()); // 已支付
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setTradeNo(resultMap.get("transaction_id")); //交易编号，微信查询支付状态接口返回的map中会包含(键是transaction_id)
        paymentInfo.setCallbackContent(resultMap.toString());
        baseMapper.updateById(paymentInfo);

        //3 根据订单号得到订单信息
        //4 更新订单信息(yygh_order库-order_info表)
        OrderInfo orderInfo = orderService.getById(paymentInfo.getOrderId());
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus()); // 已支付
        orderService.updateById(orderInfo);

        //5 调用医院接口，更新订单支付信息(yygh_manage库-order_info表)
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode()); //获取医院签名信息(api基础路径、签名秘钥)
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode",orderInfo.getHoscode());
        reqMap.put("hosRecordId", orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);
        //signInfoVo.getApiUrl()就是对接了我们平台的医院开放给我们的医院接口模拟系统api地址，这里是协和医院(http://localhost:9998)
         JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl() + "/order/updatePayStatus");
//        JSONObject result = HttpRequestHelper.sendRequest(reqMap,  "http://localhost:9998/order/updatePayStatus");
        if(result.getInteger("code") != 200) {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
    }
}
