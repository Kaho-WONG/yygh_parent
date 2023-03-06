package com.kaho.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaho.common.rabbit.constant.MqConst;
import com.kaho.common.rabbit.service.RabbitService;
import com.kaho.yygh.common.exception.YyghException;
import com.kaho.yygh.common.helper.HttpRequestHelper;
import com.kaho.yygh.common.result.ResultCodeEnum;
import com.kaho.yygh.enums.OrderStatusEnum;
import com.kaho.yygh.hosp.client.HospitalFeignClient;
import com.kaho.yygh.model.order.OrderInfo;
import com.kaho.yygh.model.user.Patient;
import com.kaho.yygh.order.mapper.OrderMapper;
import com.kaho.yygh.order.service.OrderService;
import com.kaho.yygh.user.client.PatientFeignClient;
import com.kaho.yygh.vo.hosp.ScheduleOrderVo;
import com.kaho.yygh.vo.msm.MsmVo;
import com.kaho.yygh.vo.order.OrderMqVo;
import com.kaho.yygh.vo.order.SignInfoVo;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @description:
 * @author: Kaho
 * @create: 2023-03-05 22:36
 **/
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderInfo> implements OrderService {

    @Autowired
    private PatientFeignClient patientFeignClient;

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Autowired
    private RabbitService rabbitService;

    //保存生成挂号订单
    @Override
    public Long saveOrder(String scheduleId, Long patientId) {

        //获取就诊人信息
        Patient patient = patientFeignClient.getPatientOrder(patientId);

        //获取排班相关信息
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);

        //判断当前时间是否还可以预约
        if(new DateTime(scheduleOrderVo.getStartTime()).isAfterNow()
                || new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()) {
            throw new YyghException(ResultCodeEnum.TIME_NO);
        }

        //获取签名信息
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(scheduleOrderVo.getHoscode());

        //添加到订单表
        OrderInfo orderInfo = new OrderInfo();
        //scheduleOrderVo 数据复制到 orderInfo
        BeanUtils.copyProperties(scheduleOrderVo,orderInfo);
        //向orderInfo设置其他数据
        String outTradeNo = System.currentTimeMillis() + "" + new Random().nextInt(100); //订单交易号
        orderInfo.setOutTradeNo(outTradeNo);
        // 注意这里设置为医院自身的排班id，不要将唯一id设置进去了。同时OrderInfo类中映射字段改为 hos_schedule_id
        orderInfo.setScheduleId(scheduleOrderVo.getHosScheduleId());
//        orderInfo.setScheduleId(scheduleId); 错误的！！！
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setPatientId(patientId);
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus()); //状态: 预约成功，待支付
        baseMapper.insert(orderInfo); //生成初始订单，等待医院方(医院接口模拟系统)进行处理(处理就诊人信息、更新预约数、生成预约号序等)

        //调用医院接口，实现预约挂号操作
        //设置调用医院接口需要的参数，参数放到map集合
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode", orderInfo.getHoscode());
        paramMap.put("depcode", orderInfo.getDepcode());
        paramMap.put("hosScheduleId", orderInfo.getScheduleId());
        paramMap.put("reserveDate", new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", orderInfo.getReserveTime());
        paramMap.put("amount", orderInfo.getAmount());

        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType", patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex", patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone", patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode", patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode", patient.getDistrictCode());
        paramMap.put("address", patient.getAddress());
        //联系人
        paramMap.put("contactsName", patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo", patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone", patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());

        String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        paramMap.put("sign", sign);

        //请求医院系统接口: 返回的数据就是封装了可预约数量变少等信息在内的json对象，我们取出字段更新到我们数据库的排班表就可以了
        //signInfoVo.getApiUrl()就是对接了我们平台的医院开放给我们的医院接口模拟系统api地址，这里是协和医院(http://localhost:9998)
        JSONObject result = HttpRequestHelper.sendRequest(paramMap, signInfoVo.getApiUrl() + "/order/submitOrder");
        //JSONObject result = HttpRequestHelper.sendRequest(paramMap, "http://localhost:9998/order/submitOrder");

        // 如果预约成功，会返回200状态码，随后就要进行数据库更新，发送mq消息，号源更新和短信通知
        if(result.getInteger("code") == 200) {
            JSONObject jsonObject = result.getJSONObject("data");
            //预约记录唯一标识（医院预约记录主键）
            String hosRecordId = jsonObject.getString("hosRecordId");
            //预约序号
            Integer number = jsonObject.getInteger("number");;
            //取号时间
            String fetchTime = jsonObject.getString("fetchTime");;
            //取号地址
            String fetchAddress = jsonObject.getString("fetchAddress");;
            //更新订单
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);
            baseMapper.updateById(orderInfo); //往初始订单中添加新的信息

            //排班可预约数(总数)
            Integer reservedNumber = jsonObject.getInteger("reservedNumber");
            //排班剩余可预约数
            Integer availableNumber = jsonObject.getInteger("availableNumber");

            // 发送mq消息，进行号源更新和短信通知
            //号源更新信息
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(scheduleId);
            orderMqVo.setReservedNumber(reservedNumber);
            orderMqVo.setAvailableNumber(availableNumber);
            //短信提示信息
            MsmVo msmVo = new MsmVo();
            //这里因为我是用阿里云测试短信功能，只能绑定一个我自己的手机号，所以这里setPhone也只能用我的手机号(就诊人就手动选用户自己)
            msmVo.setPhone(orderInfo.getPatientPhone());
            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                    + (orderInfo.getReserveTime() == 0 ? "上午" : "下午");
            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle());
                put("amount", orderInfo.getAmount());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
                put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
                //因为我的阿里云短信服务没有开通「自定义短信模板」，只能用短信测试的验证码短信功能，所以这里上面的几行put最后都用不上
                //短信测试的param中只有 "code" 键会被aliyun-SDK识别，所以我这里只用code:8888来代表预约成功的短信提示
                put("code", "8888");
            }};
            msmVo.setParam(param);
            orderMqVo.setMsmVo(msmVo);

            //发送信息到消息队列 —— QUEUE_ORDER : 生产订单消息到订单消息队列
            //hosp模块的监听器HospitalReceiver(消费者)监听到该消息后会根据消息中的号源更新信息去mongodb中更新Schedule排班数据表
            //随后HospitalReceiver会作为生产者将消息中的短信提示信息重新封装为一条消息投递到QUEUE_MSM_ITEM短信消息队列
            //msm模块的监听器MsmReceiver(消费者)监听到短信消息队列的消息后会根据消息中的短信提示信息调用接口发送短信到用户手机号
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
        } else {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
        return orderInfo.getId();
    }
}
