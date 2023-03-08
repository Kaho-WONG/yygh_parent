package com.kaho.yygh.hosp.receiver;

import com.kaho.common.rabbit.constant.MqConst;
import com.kaho.common.rabbit.service.RabbitService;
import com.kaho.yygh.hosp.service.ScheduleService;
import com.kaho.yygh.model.hosp.Schedule;
import com.kaho.yygh.vo.msm.MsmVo;
import com.kaho.yygh.vo.order.OrderMqVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @description: mq监听器，监听队列中的消息。用户在 order模块(生产者)下单后，投递一条“预约下单”的消息，
 * 该消息会投递到 QUEUE_ORDER队列 中，等待本监听器 HospitalReceiver(消费者)监听到，然后根据消息携带
 * 的排班预约数信息去更新 MongoDB 中的 Schedule表预约数，最后再作为(生产者)向 QUEUE_MSM_ITEM队列中
 * 投递一条“发送预约成功短信”的消息，告知后面的监听器 MsmReceiver(消费者)调用阿里云短信服务发送短信到
 * 用户的手机号。
 *
 * @author: Kaho
 * @create: 2023-03-06 19:54
 **/
@Component
public class HospitalReceiver {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private RabbitService rabbitService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER),
            key = {MqConst.ROUTING_ORDER}
    ))
    public void receiver(OrderMqVo orderMqVo, Message message, Channel channel) throws IOException {
        if(null != orderMqVo.getAvailableNumber()) {
            //下单成功，更新预约数
            Schedule schedule = scheduleService.getScheduleId(orderMqVo.getScheduleId()); //这里scheduleId是从前端传来的，可以用
            schedule.setReservedNumber(orderMqVo.getReservedNumber());
            schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
            scheduleService.update(schedule); //在mongodb中更新排班信息(剩余可预约数...)
        } else {
            //取消预约，更新预约数
            Schedule schedule = scheduleService.getScheduleId2(orderMqVo.getScheduleId());
            int availableNumber = schedule.getAvailableNumber().intValue() + 1;
            schedule.setAvailableNumber(availableNumber);
            scheduleService.update(schedule);
        }
        //发送短信(预约成功/取消预约短信，具体根据状态码区别: 8888-预约成功 5555-取消预约)
        MsmVo msmVo = orderMqVo.getMsmVo();
        if(null != msmVo) {
            //这里将消息投递到QUEUE_MSM_ITEM队列中，等待service_msm服务中的MsmReceiver监听器收到后触发发送短信操作
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }
    }

}
