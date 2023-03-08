package com.kaho.yygh.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kaho.yygh.model.order.OrderInfo;
import com.kaho.yygh.vo.order.OrderCountQueryVo;
import com.kaho.yygh.vo.order.OrderCountVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description:
 * @author: Kaho
 * @create: 2023-03-05 22:35
 **/
public interface OrderMapper extends BaseMapper<OrderInfo> {

    //查询预约统计数据的方法
    List<OrderCountVo> selectOrderCount(@Param("vo") OrderCountQueryVo orderCountQueryVo);
}

