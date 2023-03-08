package com.kaho.yygh.order.receiver;

import com.kaho.common.rabbit.constant.MqConst;
import com.kaho.yygh.order.service.OrderService;
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
 * @description: rabbitmq 监听器
 * 专门监听由定时任务模块 service_task 在每天 8 点投递到「定时任务」mq 队列的消息，
 * 然后触发调用OrderService.patientTips()接口的操作，这个接口内部会作为生产者
 * 再投递一条消息到「短信」mq 队列，告知短信服务模块 service_msm 调用阿里云短信服务
 * 发送就诊提醒短信给就诊安排日期是当天的就诊人。
 * @author: Kaho
 * @create: 2023-03-08 16:48
 **/
@Component
public class OrderReceiver {

    @Autowired
    private OrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_8, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_8}
    ))
    public void patientTips(Message message, Channel channel) throws IOException {
        //消费者: 监听到来自「定时任务」队列的消息后，执行patientTips();
        System.out.println(message);
        orderService.patientTips(); //生产者: 方法内部作为生产者向「短信」队列投递一条「发送就诊提醒短信」的消息
    }
}
