package com.pinyougou.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    /**
     * 基于安全框架获取用户信息
     * {loginName:"admin"}
     */
    @RequestMapping("/getName")
    public Map<String,String> getName(){
        //基于安全框架获取用户信息
        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String,String> map = new HashMap<>();
        map.put("loginName",loginName);
        //{loginName:"admin"}
        return map;
    }

}
