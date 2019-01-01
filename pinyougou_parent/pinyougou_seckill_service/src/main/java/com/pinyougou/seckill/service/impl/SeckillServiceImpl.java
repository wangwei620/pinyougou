package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillService;
import com.pinyougou.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SeckillServiceImpl implements SeckillService{

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ThreadPoolTaskExecutor executor;
    @Autowired
    private CreateSeckillOrder createSeckillOrder;
    /**
     * 从redis 中查询所有要参加秒杀的商品
     * @return
     */
    @Override
    public List<TbSeckillGoods> findAllSeckillGoodsFromRedis() {
        //获取redis中的数据
        List seckill_goods = redisTemplate.boundHashOps("seckill_goods").values();
        return seckill_goods;
    }

    /**
     * 查找指定的商品详情
     * @param seckillGoodsId
     * @return
     */
    @Override
    public TbSeckillGoods findOneSeckillGoodsFromRedis(Long seckillGoodsId) {
        //通过redis中获取数据
       return (TbSeckillGoods) redisTemplate.boundHashOps("seckill_goods").get(seckillGoodsId);

    }
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    /**
     * 保存秒杀订单
     * @param seckillGoodsId
     * @param userId
     */
    @Override
    public void saveSeckillOrder(Long seckillGoodsId, String userId) {


        //解决同一个人重复抢购的问题
        Boolean member = redisTemplate.boundSetOps("seckill_goods_" + seckillGoodsId).isMember(userId);
        if(member){
            //如果能取到,说明,重复购买了
            throw new RuntimeException("您已经抢购成功,不能重复购买");
        }
        //解决商品超卖的问题
        //右弹栈
        Object obj = redisTemplate.boundListOps("seckill_goods_queue" + seckillGoodsId).rightPop();
        if (obj==null){
            throw new RuntimeException("商品售完");
        }

        //从缓存中获取秒杀商品
        TbSeckillGoods seckillGoods=  (TbSeckillGoods) redisTemplate.boundHashOps("seckill_goods").get(seckillGoodsId);

        //排队人数过多提醒操作
        Long size = redisTemplate.boundValueOps("seckill_goods_paixu").size();
        if (seckillGoods.getStockCount()+10<size){
            throw new RuntimeException("当前人数排队过多,请稍后再试");
        }
        //排队人数优化,提醒  每次都加一操作,到redis中
        redisTemplate.boundValueOps("seckill_goods_paixu"+seckillGoodsId).increment(1);

        //=========================================================

        //将秒杀下单的任务存到缓存中,然后在订单的缓存中,获得下单任务,执行保存订单操作
        Map<String,Object> param = new HashMap<>();
        param.put("seckillGoodsId",seckillGoodsId);
        param.put("userId",userId);
        //存入redis中
        redisTemplate.boundListOps("seckill_order_queue").leftPush(param);
        //调用线程的方法
        executor.execute(createSeckillOrder);//里面是接口,但是我们给一个子实现类,也是可以的
    }
}
