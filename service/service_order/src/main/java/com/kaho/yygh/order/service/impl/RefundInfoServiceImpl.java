package com.kaho.yygh.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaho.yygh.enums.RefundStatusEnum;
import com.kaho.yygh.model.order.PaymentInfo;
import com.kaho.yygh.model.order.RefundInfo;
import com.kaho.yygh.order.mapper.RefundInfoMapper;
import com.kaho.yygh.order.service.RefundInfoService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @description:
 * @author: Kaho
 * @create: 2023-03-08 12:08
 **/
@Service
public class RefundInfoServiceImpl
        extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {

    //保存退款记录
    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {
        //判断是否有重复数据添加
        QueryWrapper<RefundInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", paymentInfo.getOrderId());
        wrapper.eq("payment_type", paymentInfo.getPaymentType());
        RefundInfo refundInfo = baseMapper.selectOne(wrapper);
        if(refundInfo != null) { //有相同数据
            return refundInfo;
        }
        //没有重复记录，添加记录
        refundInfo = new RefundInfo();
        refundInfo.setCreateTime(new Date());
        refundInfo.setOrderId(paymentInfo.getOrderId());
        refundInfo.setPaymentType(paymentInfo.getPaymentType());
        refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus()); // 状态: 退款中
        refundInfo.setSubject(paymentInfo.getSubject()); // 交易内容
        refundInfo.setTotalAmount(paymentInfo.getTotalAmount()); // 退款金额
        baseMapper.insert(refundInfo);
        return refundInfo;
    }
}
