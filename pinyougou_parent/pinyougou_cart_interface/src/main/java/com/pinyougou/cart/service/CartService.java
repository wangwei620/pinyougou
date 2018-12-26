package com.pinyougou.cart.service;

import com.pinyougou.groupentity.Cart;

import java.util.List;

public interface CartService {

    /**
     * 添加商品到购物车
     */
    public List<Cart> addItemToCartList(List<Cart> cartList, Long itemId, Integer num);

    /**
     * 根据sessionId获得购物车列表
     * @param sessionId
     * @return
     */
    List<Cart> selectCartListFromRedis(String sessionId);

    /**
     * 保存该sessionid 的 cartList 购物车列表
     * @param sessionId
     * @param cartList
     */
    void saveCartListToRedis(String sessionId, List<Cart> cartList);

    /**
     * 合并添加前购物车的数据到登陆后的购物车列表
     * @param cartList_sessionId
     * @param cartList_username
     * @return
     */
    List<Cart> mergeCartList(List<Cart> cartList_sessionId, List<Cart> cartList_username);

    /**
     * 删除之前未登陆的购物车的列表数据
     * @param cartList_sessionId
     */
    void deleteCartList(List<Cart> cartList_sessionId);
}
