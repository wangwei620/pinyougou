package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.groupentity.Cart;
import com.pinyougou.pojo.Result;
import com.pinyougou.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private HttpSession session;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    @Reference
    private CartService cartService;
    /**
     * 获得sessionid的方法
     */
    private String getSessionId(){

        //先尝试从"cartCookie"中获得sessionId信息
        String sessionId = CookieUtil.getCookieValue(request, "cartList", "utf-8");
        if (sessionId==null){
            //在从浏览器中获取sessionid
             sessionId = session.getId();
             //将浏览器获取的sessionId保存一周
            CookieUtil.setCookie(request,response,"cartList",sessionId,3600*24*7,"utf-8");
        }
        return sessionId;
    }
    /**
     * 展示购物车列表数据
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        //获取登陆人用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //未登陆时,基于sessionid从redis中获取购物车数据列表
        String sessionId = getSessionId();
        //从redis中获取
        List<Cart>  cartList_sessionId =  cartService.selectCartListFromRedis(sessionId);
        if ("anonymousUser".equals(username)){//未登陆
            System.out.println("selectCartListFromRedis by sessionId....");
            return cartList_sessionId;
        }else{//已登录
            System.out.println("selectCartListFromRedis by username....");
            List<Cart>  cartList_username =  cartService.selectCartListFromRedis(username);
            //用户登录前，如果已经添加商品到购物车列表中。
            if(cartList_sessionId!=null&&cartList_sessionId.size()>0){//说明添加前已经存在商品了
                //登陆后,将登陆前的购物车列表数据合并到登陆后的购物车列表中
             cartList_username = cartService.mergeCartList(cartList_sessionId,cartList_username);
             //将合并后的结果从新放到缓存中
                cartService.saveCartListToRedis(username,cartList_username);
                //清除合并前的购物车列表数据
                cartService.deleteCartList(cartList_sessionId);
            }
            return cartList_username;
        }

    }

    /**
     * 添加商品到购物车
     */
    @RequestMapping("/addItemToCartList")
    @CrossOrigin(origins = "http://item.pinyougou.com",allowCredentials = "true")
    public Result addItemToCartList(Long itemId, Integer num){
        try {

            //获取登陆人用户名
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println(username);


            //1.查询购物车列表
            List<Cart> cartList = findCartList();
            //2.添加商品到购物车
            cartList =  cartService.addItemToCartList(cartList,itemId, num);
            if ("anonymousUser".equals(username)){//未登陆
                System.out.println("saveCartListToRedis by sessionId.....");
                //获取sessionId
                String sessionId = getSessionId();
                //3.保存购物车列表到redis中
                cartService.saveCartListToRedis(sessionId,cartList);
            }else{//已登录
                System.out.println("saveCartListToRedis by username.....");
                cartService.saveCartListToRedis(username,cartList);

            }

            return new Result(true,"添加购物车成功");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加购物车失败");
        }
    }

}
