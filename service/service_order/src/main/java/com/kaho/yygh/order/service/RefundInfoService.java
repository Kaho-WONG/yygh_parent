package com.kaho.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaho.yygh.model.order.PaymentInfo;
import com.kaho.yygh.model.order.RefundInfo;

/**
 * @description:
 * @author: Kaho
 * @create: 2023-03-08 12:08
 **/
public interface RefundInfoService extends IService<RefundInfo> {
    /**
     * 保存退款记录
     * @param paymentInfo
     */
    RefundInfo saveRefundInfo(PaymentInfo paymentInfo);
}

