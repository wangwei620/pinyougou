package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Result;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seckill")
public class SeckillController {


    @Reference
    private SeckillService seckillService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 从redis中查询所有的要参加秒杀的商品
     * @return
     */
    @RequestMapping("/findSeckillList")
    public List<TbSeckillGoods> findSeckillList(){
     return   seckillService.findAllSeckillGoodsFromRedis();
    }
    /**
     * 从redis中查询选中的秒杀的商品
     */
    @RequestMapping("/findOneSeckillGoods")
    public TbSeckillGoods findOneSeckillGoods(Long seckillGoodsId){

      return  seckillService.findOneSeckillGoodsFromRedis(seckillGoodsId);
    }
    /**
     * 保存秒杀的订单
     */
    @RequestMapping("/saveSeckillOrder")
    public Result saveSeckillOrder(Long seckillGoodsId){
        try {

            //基于安全获取登录人信息
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();


            if(userId.equals("anonymousUser")){
                return  new Result(false,"请下登录，再下单");
            }

            seckillService.saveSeckillOrder(seckillGoodsId,userId);
            return new Result(true,"秒杀下单成功");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"秒杀下单失败");
        }
    }

}
