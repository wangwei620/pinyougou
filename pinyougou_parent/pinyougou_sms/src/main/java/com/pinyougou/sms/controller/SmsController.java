package com.pinyougou.sms.controller;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.pinyougou.sms.util.SmsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("sms")
public class SmsController {

    @Autowired
    private SmsUtil smsUtil;

    /**
     * 调用工具发短信
     */
    @RequestMapping(value = "sendSms" ,method = RequestMethod.POST)
    public Map<String,String> sendSms(String phoneNumbers,String signName,String templateCode,String param){
        //调用工具类发短信
        try {
            //调用接口
            SendSmsResponse response = smsUtil.sendSms(phoneNumbers, signName, templateCode, param);
            //输出需要的信息
            //包括 code  Message    requestId   Bizid
            System.out.println("Code=" + response.getCode());
            System.out.println("Message=" + response.getMessage());
            System.out.println("RequestId=" + response.getRequestId());
            System.out.println("BizId=" + response.getBizId());
            //封装数据
            Map<String,String> resultMap = new HashMap<>();
            resultMap.put("Code",response.getCode());
            resultMap.put("Message",response.getMessage());
            resultMap.put("RequestId",response.getRequestId());
            resultMap.put("BizId",response.getBizId());
            //返回信息调用接口的信息
            return resultMap;

        } catch (ClientException e) {
            e.printStackTrace();
            return null;
        }
    }
}
