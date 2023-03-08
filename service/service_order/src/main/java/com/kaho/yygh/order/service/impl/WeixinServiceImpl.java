package com.kaho.yygh.order.service.impl;

import com.github.wxpay.sdk.WXPayUtil;
import com.kaho.yygh.enums.PaymentTypeEnum;
import com.kaho.yygh.model.order.OrderInfo;
import com.kaho.yygh.order.service.OrderService;
import com.kaho.yygh.order.service.PaymentService;
import com.kaho.yygh.order.service.WeixinService;
import com.kaho.yygh.order.utils.ConstantPropertiesUtils;
import com.kaho.yygh.order.utils.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: Kaho
 * @create: 2023-03-07 16:57
 **/
@Service
public class WeixinServiceImpl implements WeixinService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RedisTemplate redisTemplate;

    // 根据订单号下单，生成微信支付连接(二维码)
    @Override
    public Map createNative(Long orderId) {
        try {
            //从redis获取数据，如果redis有，代表前面生成微信支付的二维码还未失效。直接返回即可不用再生成
            Map payMap = (Map)redisTemplate.opsForValue().get(orderId.toString());
            if(payMap != null) {
                return payMap;
            }
            //1 根据orderId获取订单信息
            OrderInfo order = orderService.getById(orderId);

            //2 向支付记录表添加信息 (支付方式: 采取微信支付)
            paymentService.savePaymentInfo(order, PaymentTypeEnum.WEIXIN.getStatus());

            //3 设置必要参数
            //把参数转换xml格式，使用商户key进行加密
            Map paramMap = new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID); //关联的公众号appid
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER); //商户号
            //用微信支付的工具类生成唯一的字符串
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            String body = order.getReserveDate() + "就诊" + order.getDepname(); // “安排日期+就诊+科室名称”
            paramMap.put("body", body);
            paramMap.put("out_trade_no", order.getOutTradeNo()); //订单交易号
            //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+""); 100元
            //订单金额，实际开发应该是上面这行代码，得到订单金额传进去，为了测试，统一写成这个值，即 1分钱
            paramMap.put("total_fee", "1");
            paramMap.put("spbill_create_ip", "127.0.0.1");
            paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
            //NATIVE代表是微信扫码支付
            paramMap.put("trade_type", "NATIVE");

            //4 调用微信生成二维码接口，通过 httpclient 工具类调用
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //设置map参数  WXPayUtil.generateSignedXml是微信支付SDK提供的将map转成xml的方法(第二个参数是微信商户key)
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            //我们上面的 client 请求的地址是 https://api.m...  因为是 https，所以下面要设置true表示支持
            client.setHttps(true);
            client.post(); // 发起请求，等待微信方(微信支付系统)生成预支付交易，返回预支付交易链接(code_url)

            //5 返回相关数据(以xml的形式组织，内部有预支付交易链接code_url)
            String xml = client.getContent();
            //调用微信支付SDK提供的工具类将 xml 转换成 map 集合
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            System.out.println("resultMap:" + resultMap);

            //6 封装返回结果集
            Map map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", order.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url")); //预支付交易链接，即二维码地址

            //设置生成的微信支付二维码2个小时有效！用redis存储
            if(resultMap.get("result_code") != null) {
                redisTemplate.opsForValue().set(orderId.toString(), map, 120, TimeUnit.MINUTES);
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 根据订单id去微信第三方查询支付状态
    @Override
    public Map<String, String> queryPayStatus(Long orderId) {
        try {
            //1 根据orderId获取订单信息
            OrderInfo orderInfo = orderService.getById(orderId);

            //2 封装提交参数
            Map paramMap = new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);    //关联的公众号appid
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER); //商户号
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo()); //订单交易号
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr()); //用微信支付的工具类生成唯一的字符串

            //3 设置请求内容  用 httpclient 调用微信接口查询支付状态
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();

            //4 得到微信接口返回数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            System.out.println("支付状态resultMap:" + resultMap);
            //5 把接口数据返回
            return resultMap;
        } catch(Exception e) {
            return null;
        }
    }
}
