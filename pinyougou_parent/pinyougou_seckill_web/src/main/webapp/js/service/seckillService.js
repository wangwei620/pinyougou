//服务层
app.service('seckillService',function($http){

    //查询需要秒杀的商品列表
    this.findSeckillList=function(){
        return $http.get('seckill/findSeckillList.do');
    }
    //查询指定的秒杀商品
    this.findOneSeckillGoods=function (seckillGoodsId) {
        return $http.get('seckill/findOneSeckillGoods.do?seckillGoodsId='+seckillGoodsId)
    }
    //秒杀下单
    this.saveSeckillOrder=function (seckillGoodsId) {
        return $http.get('seckill/saveSeckillOrder.do?seckillGoodsId='+seckillGoodsId)
    }
});
