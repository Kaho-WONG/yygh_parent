package com.kaho.yygh.task.scheduled;

import com.kaho.common.rabbit.constant.MqConst;
import com.kaho.common.rabbit.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @description: 定时任务类，配置定时任务
 * @author: Kaho
 * @create: 2023-03-08 16:21
 **/
@Component
@EnableScheduling
public class ScheduledTask {

    @Autowired
    private RabbitService rabbitService;

    /**
     * cron表达式，设置执行间隔
     * 每天8点执行 提醒就诊： 0 0 8 * * ?  (注意 6 位就可以了)
     *
     * 每隔 2分钟执行：0 0/2 * * * ? :
     * 2023-03-08 19:10:00
     * 2023-03-08 19:12:00
     * 2023-03-08 19:14:00
     * 2023-03-08 19:16:00
     * 2023-03-08 19:18:00
     */
    @Scheduled(cron = "0 0 8 * * ?") // 为了方便测试，我们设置每隔30秒执行一次！
    public void taskPatient() {
        System.out.println("1111");
        //执行的操作是往mq的「定时任务」队列发送消息，提醒OrderReceiver监听器生成“发送短信”消息再投递到「短信」队列
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_8, "");
    }
}
