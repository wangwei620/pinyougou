package com.pinyougou.seckill.service.impl;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.Map;

public class CreateSeckillOrder implements Runnable {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Override
    public void run() {
        
        //从redis中取出我们需要的订单任务
        Map<String,Object> param = (Map<String, Object>) redisTemplate.boundListOps("seckill_order_queue").rightPop();
        Long seckillGoodsId = (Long) param.get("seckillGoodsId");
        String userId = (String) param.get("userId");
        //从缓存中获取秒杀商品
        TbSeckillGoods seckillGoods=  (TbSeckillGoods) redisTemplate.boundHashOps("seckill_goods").get(seckillGoodsId);
        //组装秒杀订单数据
        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        /*
        tb_seckill_order
	  `id` bigint(20) NOT NULL COMMENT '主键',
	  `seckill_id` bigint(20) DEFAULT NULL COMMENT '秒杀商品ID',
	  `money` decimal(10,2) DEFAULT NULL COMMENT '支付金额',
	  `user_id` varchar(50) DEFAULT NULL COMMENT '用户',
	  `seller_id` varchar(50) DEFAULT NULL COMMENT '商家',
	  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
	  `status` varchar(1) DEFAULT NULL COMMENT '状态',
         */
        seckillOrder.setId(idWorker.nextId());
        seckillOrder.setSeckillId(seckillGoodsId);
        seckillOrder.setMoney(seckillGoods.getCostPrice());
        seckillOrder.setUserId(userId);
        seckillOrder.setSellerId(seckillGoods.getSellerId());
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setStatus("1");//未支付

        //设置秒杀商品库存减一
        seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
        //保存秒杀订单
        seckillOrderMapper.insert(seckillOrder);
        //排队人数优化,下单成功  每次都减一操作,到redis中
        redisTemplate.boundValueOps("seckill_goods_paixu"+seckillGoodsId).increment(-1);
        //秒杀下单成功后,我们要保存一份预支付的订单到redis,中,下次我们再在redis中判断,是否是第二次购买该商品
        //解决同一个人,不能重复购买的问题
        redisTemplate.boundSetOps("seckill_goods_"+seckillGoodsId).add(userId);
        if(seckillGoods.getStockCount()<=0){
            //商品售完，没有库存后，需要更新数据库中秒杀商品库存数据
            seckillGoodsMapper.updateByPrimaryKey(seckillGoods);

            //清除redis中该商品
            redisTemplate.boundHashOps("seckill_goods").delete(seckillGoodsId);
        }
        //秒选下单成功后，扣减库存
        redisTemplate.boundHashOps("seckill_goods").put(seckillGoodsId,seckillGoods);
    }
}
