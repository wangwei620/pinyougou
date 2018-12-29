package com.pinyougou.pay.service;

import com.pinyougou.pojo.TbPayLog;

import java.util.Map;

public interface PayService {
    /**
     * 调用同一接口,生成二维码 或者支付链
     * String out_trade_no  订单号
     * total_fee  总费用
     */
    public Map<String,Object> createNative(String out_trade_no,String total_fee) throws Exception;

    /**
     * 调用查询状态接口 ,
     */
    public Map queryPayStatus(String out_trade_no) throws Exception;

    TbPayLog selectPayLogFormRedis(String userId);

    void updateStatus(String out_trade_no, String transaction_id);
}
