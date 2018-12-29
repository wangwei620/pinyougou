package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pay.service.PayService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.util.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@Service
@Transactional
public class PayServiceImpl implements PayService {

    @Value("${appid}")
    private String appid;//公众号id

    @Value("${partner}")
    private String partner;//商户id

    @Value("${partnerkey}")
    private String partnerkey;//商户秘钥

    @Value("${notifyurl}")
    private String notifyurl;//回调地址

    /**
     * 调用同一下单接口
     * @param out_trade_no
     * @param total_fee
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> createNative(String out_trade_no, String total_fee) throws Exception {
        //1、组装请求参数
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("appid",appid);
        paramMap.put("mch_id",partner);
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        paramMap.put("body","品优购");
        paramMap.put("out_trade_no",out_trade_no);
        paramMap.put("total_fee",total_fee);
        paramMap.put("spbill_create_ip","127.0.0.1");
        paramMap.put("notify_url",notifyurl);
        paramMap.put("trade_type","NATIVE");
        paramMap.put("product_id","1");

        //将map组装好的参数转xml字符串
        String xmlParam = WXPayUtil.generateSignedXml(paramMap, partnerkey);
        System.out.println(xmlParam);

        //2、基于httpclient发起请求调用
        HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
        httpClient.setHttps(true);
        httpClient.setXmlParam(xmlParam);//设置请求参数
        httpClient.post();

        //3、处理响应结果
        String content = httpClient.getContent();//获取请求响应结果内容，是xml格式字符串
        System.out.println(content);
        Map<String, String> resultMap = WXPayUtil.xmlToMap(content);

        //自己组装页面所需要的参数
        Map<String,Object> map = new HashMap<>();
        map.put("code_url",resultMap.get("code_url"));
        map.put("out_trade_no",out_trade_no);
        map.put("total_fee",total_fee);

        return map;
    }

    /**
     * 调用查询状态接口
     * @param out_trade_no
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) throws Exception {
        //1.组装请求数据
        //1、组装请求参数
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("appid",appid);
        paramMap.put("mch_id",partner);
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        paramMap.put("out_trade_no",out_trade_no);
        String xmlParam = WXPayUtil.generateSignedXml(paramMap, partnerkey);
        //2.发送HttpClient请求
        HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
        httpClient.setHttps(true);
        httpClient.setXmlParam(xmlParam);//设置请求参数
        httpClient.post();
        //3.返回相应数据
        String content = httpClient.getContent();
        System.out.println(content);
        Map<String, String> xmlMap = WXPayUtil.xmlToMap(content);
        return xmlMap;
    }
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public TbPayLog selectPayLogFormRedis(String userId) {
        return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
    }
    @Autowired
    private TbPayLogMapper payLogMapper;
    @Autowired
    private TbOrderMapper orderMapper;
    /**
     * 跟新支付日志状态
     * @param out_trade_no
     * @param transaction_id
     */
    @Override
    public void updateStatus(String out_trade_no, String transaction_id) {
        //跟新日志状态
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        payLog.setPayTime(new Date());
        payLog.setTradeState("2");
        payLog.setTransactionId(transaction_id);
        payLogMapper.updateByPrimaryKey(payLog);

        //跟新订单状态

        String orderList = payLog.getOrderList();
        String[] split = orderList.split(",");
        for (String orderId : split) {
            TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
            tbOrder.setStatus("2");//已支付
            tbOrder.setPaymentTime(new Date());
        }
        //清除当前redis中关联支付日志的信息
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
    }
}
