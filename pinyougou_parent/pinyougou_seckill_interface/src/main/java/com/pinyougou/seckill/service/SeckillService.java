package com.pinyougou.seckill.service;

import com.pinyougou.pojo.TbSeckillGoods;

import java.util.List;

public interface SeckillService {

    /**
     * 从redis中查询所有的秒杀商品列表
     */
    public List<TbSeckillGoods> findAllSeckillGoodsFromRedis();

    /**
     * 查找指定商品的详情页面
     */
    public  TbSeckillGoods findOneSeckillGoodsFromRedis(Long seckillGoodsId);

    /**
     * 秒杀下单操作
     */
    public void saveSeckillOrder(Long seckillGoodsId,String userId);
}
