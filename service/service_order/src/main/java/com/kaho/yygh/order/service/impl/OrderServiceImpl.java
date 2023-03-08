package com.kaho.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.kaho.yygh.order.service.WeixinService;
import com.kaho.yygh.user.client.PatientFeignClient;
import com.kaho.yygh.vo.hosp.ScheduleOrderVo;
import com.kaho.yygh.vo.msm.MsmVo;
import com.kaho.yygh.vo.order.OrderMqVo;
import com.kaho.yygh.vo.order.OrderQueryVo;
import com.kaho.yygh.vo.order.SignInfoVo;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @description: 订单服务 service 接口
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

    @Autowired
    private WeixinService weixinService;

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

        //获取医院签名信息
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

    
    //订单列表（条件查询带分页）
    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo) {
        //orderQueryVo获取模糊查询的条件值
        String name = orderQueryVo.getKeyword(); //医院名称
        Long patientId = orderQueryVo.getPatientId(); //就诊人id
        String patientName = orderQueryVo.getPatientName(); //就诊人名字
        String orderStatus = orderQueryVo.getOrderStatus(); //订单状态
        String reserveDate = orderQueryVo.getReserveDate();//安排时间
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();

        //对条件值进行非空判断
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(name)) {
            wrapper.like("hosname", name);
        }
        if(!StringUtils.isEmpty(patientId)) {
            wrapper.eq("patient_id", patientId);
        }
        if(!StringUtils.isEmpty(patientName)) {
            wrapper.like("patient_name", patientName);
        }
        if(!StringUtils.isEmpty(orderStatus)) {
            wrapper.eq("order_status", orderStatus);
        }
        if(!StringUtils.isEmpty(reserveDate)) {
            wrapper.ge("reserve_date", reserveDate);
        }
        if(!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time", createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time", createTimeEnd);
        }
        //调用mapper的方法
        IPage<OrderInfo> pages = baseMapper.selectPage(pageParam, wrapper);
        //编号变成对应值封装，将订单状态的枚举值封装进 orderInfo
        pages.getRecords().stream().forEach(item -> {
            this.packOrderInfo(item);
        });
        return pages;
    }

    //根据订单id查询订单详情
    @Override
    public OrderInfo getOrder(String orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        return this.packOrderInfo(orderInfo);
    }

    // 将订单状态的枚举值封装进 orderInfo
    private OrderInfo packOrderInfo(OrderInfo orderInfo) {
        orderInfo.getParam().put("orderStatusString", OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }

    
    /**
     * 订单详情 管理平台用
     * @param orderId
     * @return
     */
    @Override
    public Map<String, Object> show(Long orderId) {
        Map<String, Object> map = new HashMap<>();
        OrderInfo orderInfo = this.packOrderInfo(this.getById(orderId)); //订单信息
        map.put("orderInfo", orderInfo);
        Patient patient =  patientFeignClient.getPatientOrder(orderInfo.getPatientId()); //就诊人信息
        map.put("patient", patient);
        return map;
    }


    //取消预约
    @Override
    public Boolean cancelOrder(Long orderId) {
        //获取订单信息
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        //获取可退号时间，若当前时间大约可退号时间，不能取消预约
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());
        if(quitTime.isBeforeNow()) {
            throw new YyghException(ResultCodeEnum.CANCEL_ORDER_NO);
        }

        //调用医院方医院接口实现预约取消
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode()); //获取医院签名信息
        if(null == signInfoVo) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode", orderInfo.getHoscode());
        reqMap.put("hosRecordId", orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);
        //调用医院开放接口，让医院方先进行yygh_manage库的order_info表中相关订单的更新
        JSONObject result = HttpRequestHelper.sendRequest(reqMap,
                signInfoVo.getApiUrl() + "/order/updateCancelStatus");
        System.err.println(signInfoVo.getApiUrl() + "/order/updateCancelStatus");

        //根据医院接口返回数据，进行进一步相关操作
        if(result.getInteger("code") != 200) { //请求失败
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        } else {
            //请求成功
            //判断当前订单是否处于已支付状态，是的话要调用微信退款方法进行退款，未支付的话跳过下面直接返回true成功取消预约
            if(orderInfo.getOrderStatus().intValue() == OrderStatusEnum.PAID.getStatus().intValue()) {
                //调用微信退款方法
                Boolean isRefund = weixinService.refund(orderId);
                if(!isRefund) {
                    //退款失败
                    throw new YyghException(ResultCodeEnum.CANCEL_ORDER_FAIL);
                }
                //退款成功，需要去更新订单状态(yygh_order库order_info表的记录改为“取消预约”状态)
                orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
                baseMapper.updateById(orderInfo);

                //发送mq消息，更新mongodb中schedule表的预约数量，不设置可预约数与剩余预约数，接收端可预约数+1即可
                OrderMqVo orderMqVo = new OrderMqVo();
                orderMqVo.setScheduleId(orderInfo.getScheduleId()); //排班id
                //短信提示
                MsmVo msmVo = new MsmVo();
                //这里因为我是用阿里云测试短信功能，只能绑定一个我自己的手机号，所以这里setPhone也只能用我的手机号(就诊人就手动选用户自己)
                msmVo.setPhone(orderInfo.getPatientPhone());
                String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime()==0 ? "上午": "下午");
                Map<String,Object> param = new HashMap<String,Object>(){{
                    put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                    put("reserveDate", reserveDate);
                    put("name", orderInfo.getPatientName());
                    //因为我的阿里云短信服务没有开通「自定义短信模板」，只能用短信测试的验证码短信功能，所以这里上面的几行put最后都用不上
                    //短信测试的param中只有 "code" 键会被aliyun-SDK识别，所以我这里只用code:5555来代表取消预约成功的短信提示
                    put("code", "5555");
                }};
                msmVo.setParam(param);
                orderMqVo.setMsmVo(msmVo);
                rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
            }
        }
        return true;
    }
    
    
}
