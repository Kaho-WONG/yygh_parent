package com.kaho.yygh.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kaho.yygh.model.order.OrderInfo;
import com.kaho.yygh.vo.order.OrderQueryVo;

import java.util.Map;

/**
 * @description:
 * @author: Kaho
 * @create: 2023-03-05 22:36
 **/
public interface OrderService extends IService<OrderInfo> {

    //保存生成挂号订单
    Long saveOrder(String scheduleId, Long patientId);

    //订单列表（条件查询带分页）
    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo);

    //根据订单id查询订单详情
    OrderInfo getOrder(String orderId);

    /**
     * 订单详情 管理平台用
     * @param orderId
     * @return
     */
    Map<String, Object> show(Long orderId);

    //取消预约
    Boolean cancelOrder(Long orderId);
}
