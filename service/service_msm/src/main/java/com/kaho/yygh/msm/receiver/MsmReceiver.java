package com.kaho.yygh.msm.receiver;

import com.kaho.common.rabbit.constant.MqConst;
import com.kaho.yygh.msm.service.MsmService;
import com.kaho.yygh.vo.msm.MsmVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description: mq监听器，监听队列中的消息。
 * 收到来自 HospitalReceiver 监听器投递到 QUEUE_MSM_ITEM队列 的消息后触发消费者操作(发送短信)
 *
 * @author: Kaho
 * @create: 2023-03-06 17:38
 **/
@Component
public class MsmReceiver {

    @Autowired
    private MsmService smsService;

    //绑定队列、交换机、路由键，进行监听
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_MSM_ITEM, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_MSM),
            key = {MqConst.ROUTING_MSM_ITEM}
    ))
    public void send(MsmVo msmVo, Message message, Channel channel) {
        smsService.send(msmVo);
    }
}
