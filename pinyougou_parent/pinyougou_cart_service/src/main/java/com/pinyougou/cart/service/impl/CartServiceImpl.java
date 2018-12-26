package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.groupentity.Cart;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 添加商品到购物车
     * @param cartList
     * @param itemId
     * @param num
     * @return
     *  首先根据商品的id查询   该商家是否存在于购物车中
                 1.商家对应的购物车不存在,购物车列表中
                           创建购物车对象,在存入购物车列表中
                            创建购物车对象时,指定该购物车的商家信息,以及构建购物车明细对象和购物车明细列表
                            将购物车的明细对象添加到购物车明细列表中,将购物车明细列表添加到购物车对象,在将
                             购物车对象添加到购物车列表中
                2.商家对应的购物车对象存在于购物车列表中
                           判断该商品是否存在于该购物车商品的明细列表中
                                1.如果该商品不存在购物车明细列表中
                                          则创建购物车明细对象,在添加到购物车明细里列表中
                                2.如果该商品存在购物车明细列表中
                                         修改购物车明细对象的数量和小计金额
     */
    @Override
    public List<Cart> addItemToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //首先根据商品的id查询   该商家是否存在于购物车中
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        //优化操作,添加购物车的时候,刚好该商品下架了

        if(item==null){
            throw new RuntimeException("商品不存在");
        }

        //如果商品的状态不为1则无效
        if (!item.getStatus().equals("1")){
            throw  new RuntimeException("商品无效");
        }
        String sellerId = item.getSellerId();
        //在购物车列表中基于商品id 查询购物车对象
        Cart cart = searchCartBySellerId(cartList,sellerId);
        //判断购物车是否在空
        if (cart==null){//购物车为空
            //创建购物车对象,
            cart = new Cart();
            //指定该购物车的商家信息,以及构建购物车明细对象和购物车明细列表
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            //以及构建购物车明细对象和购物车明细列表
            List<TbOrderItem> orderItemList = new ArrayList<>();
            //构建商品明细对象
            TbOrderItem orderItem = createOrderItem(item,num);
            //购物车的明细对象添加到购物车明细列表中
            orderItemList.add(orderItem);
            //购物车明细列表添加到购物车对象
            cart.setOrderItemList(orderItemList);
            //购物车对象添加到购物车列表中
            cartList.add(cart);
        }else{//该商品的存在
            //先通过购物车获取当前购物车的商品明细对象
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            //判断该商品是否存在于该购物车商品的明细列表中
            TbOrderItem orderItem = searchOrderIdByItemId(orderItemList,itemId);
            if (orderItem==null){//在商品列表中不存在
                //如果该商品不存在购物车明细列表中
                //则创建购物车明细对象,在添加到购物车明细里列表中
                orderItem = createOrderItem(item,num);
                //在添加到购物车明细里列表中
                orderItemList.add(orderItem);
            }else{//存在于商品列表中
                //修改购物车明细对象的数量和小计金额
                orderItem.setNum(orderItem.getNum()+num);
                //小计金额
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));

                //如果商品数量小于1 则删除该商品
                if(orderItem.getNum()<1){
                    orderItemList.remove(orderItem);
                }
                //如果购物车商品明细列表中没有商品了, 则直接从购物车里列表中移除
                if (orderItemList.size()<=0){
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }



    //判断该商品是否存在于该购物车商品的明细列表中
    private TbOrderItem searchOrderIdByItemId(List<TbOrderItem> orderItems, Long itemId) {
        for (TbOrderItem orderItem : orderItems) {
            if(orderItem.getItemId().longValue()==itemId.longValue()){//存在商品列表中
                return orderItem;
            }
        }
        return null;
    }

    //创建商品的明细对象
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        //优化操作
        //创建商品的数量如果为负数
        if(num<1){
            throw new RuntimeException("新添加商品到购物车,商品数量不能小于1");
        }
        /*
          `item_id` bigint(20) NOT NULL COMMENT '商品id',
          `goods_id` bigint(20) DEFAULT NULL COMMENT 'SPU_ID',
          `title` varchar(200) COLLATE utf8_bin DEFAULT NULL COMMENT '商品标题',
          `price` decimal(20,2) DEFAULT NULL COMMENT '商品单价',
          `num` int(10) DEFAULT NULL COMMENT '商品购买数量',
          `total_fee` decimal(20,2) DEFAULT NULL COMMENT '商品总金额',
          `pic_path` varchar(200) COLLATE utf8_bin DEFAULT NULL COMMENT '商品图片地址',
          `seller_id` varchar(100) COLLATE utf8_bin DEFAULT NULL,
         */
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setItemId(item.getId());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setTitle(item.getTitle());
        orderItem.setPrice(item.getPrice());
        orderItem.setNum(num);
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        orderItem.setPicPath(item.getImage());
        orderItem.setSellerId(item.getSellerId());
        return orderItem;
    }

    //根据商品的id查询是否存在购物车对象
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }
    //通过sessionid获得购物车列表
    @Override
    public List<Cart> selectCartListFromRedis(String sessionId) {
        //从redis中获取
        List<Cart> cartList = (List<Cart>) redisTemplate.boundValueOps(sessionId).get();
        //判断cartList是否为空,因为你要返回一个list列表,如果为空,前台就不能通过fastJson解析了
        if (cartList==null){
            //为空我们可以直接从新创建一个新的arrayList
            cartList = new ArrayList<>();
        }
        return cartList;
    }
    //保存购物车列表到redis中
    @Override
    public void saveCartListToRedis(String sessionId, List<Cart> cartList) {
        redisTemplate.boundValueOps(sessionId).set(cartList,7L, TimeUnit.DAYS);
    }
    //合并登陆前购物车的数据到登陆后的购物车数据中
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList_sessionId, List<Cart> cartList_username) {
        //注意我们只需要构建我们需要添加的数据,因为我们在上面已经做过判断,我们只需要,拼装数据即可
        for (Cart cart : cartList_sessionId) {
            //获取购车列表
            List<TbOrderItem> itemList = cart.getOrderItemList();
            for (TbOrderItem orderItem : itemList) {
                //获取num和ItemId的值
                Integer num = orderItem.getNum();
                Long itemId = orderItem.getItemId();
                //购物车列表我们直接用登陆的就可以了
                cartList_username=   addItemToCartList(cartList_username,itemId,num);
            }
        }
        return cartList_username;
    }
    //删除登陆前购物车的数据
    @Override
    public void deleteCartList(List<Cart> cartList_sessionId) {
        redisTemplate.delete(cartList_sessionId);
    }
}
