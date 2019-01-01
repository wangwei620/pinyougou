package com.pinyougou.seckill.task;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class SeckillTask {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Scheduled(cron = "0/45 * * * * ?")//每30秒执行一次
    public  void synchronizeSeckillGoodsToRedis(){

        //1.查询需要秒杀的商品
        //我们把符合的商品都查询出来放到redis中
        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        example.createCriteria().andStatusEqualTo("1")
                .andStartTimeLessThanOrEqualTo(new Date())
                .andEndTimeGreaterThanOrEqualTo(new Date())
                .andStockCountGreaterThan(0);
        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);

        //2.将查询的商品的数据放到redis中
        //我们将查询符合的数据放到hash格式放到redis中
        for (TbSeckillGoods seckillGoods : seckillGoodsList) {
            redisTemplate.boundHashOps("seckill_goods").put(seckillGoods.getId(),seckillGoods);
            //商品的详情页,我们可以通过商品的id取值,如下就是的
           // List seckill_goods = redisTemplate.boundHashOps("SECKILL_GOODS").values();
        }
        System.out.println("synchronizeSeckillGoodsToRedis  worker  finished...");
    }

}
