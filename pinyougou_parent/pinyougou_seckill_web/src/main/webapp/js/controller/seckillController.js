 //控制层 
app.controller('seckillController' ,function($scope,$controller,$location ,$interval  ,seckillService){
	
	$controller('baseController',{$scope:$scope});//继承
    //,$location 跨域
    //查询需要从redis中获得需要的秒杀商品数据
    $scope.findSeckillList=function () {
        seckillService.findSeckillList().success(function (response) {
            $scope.seckillList=response;
        })
    }
    //查询秒杀详情
    $scope.findOneSeckillGoods=function () {
        //获取秒杀商品的id
        $scope.seckillGoodsId =$location.search()["seckillGoodsId"];
        seckillService.findOneSeckillGoods( $scope.seckillGoodsId).success(function (response) {
            $scope.seckillGoods=response;
            //计算出剩余时间
            //结束时间距离1970年的时间值
            var endTime = new Date($scope.seckillGoods.endTime).getTime();
            var nowTime = new Date().getTime();
            //剩余时间  注意向下取整
            $scope.secondes=Math.floor((endTime-nowTime)/1000);
            //设置一个定时器
            var time = $interval(function () {
                if ($scope.secondes>0){
                    //时间递减
                    $scope.secondes--;
                    //时间格式化
                    $scope.timeString=$scope.convertTimeString($scope.secondes);
                }else{
                    //结束时间的递减
                    $interval.cancel(time);
                }
                
            },1000);//每秒改变一些
        })
    }
    //时间的格式化
    $scope.convertTimeString=function (allseconds) {
        //计算天数
        var days = Math.floor((allseconds)/(60*60*24));
        //小时
        var hours = Math.floor((allseconds-(days*60*60*24))/(60*60));
        //分钟
        var minutes = Math.floor((allseconds-(hours*60*60)-(days*60*60*24))/60);
        //秒
        var seconds = Math.floor(allseconds-(hours*60*60)-(days*60*60*24)-(minutes*60));
        //拼接时间

        var timeString = "";
        if (days>0){
            timeString+=days+"天:";
        }

        if (hours<10){
            hours="0"+hours;
        }

        if (minutes<10){
            hours="0"+minutes;
        }
        if (seconds<10){
            seconds="0"+minutes;
        }

        return timeString+=hours+":"+minutes+":"+seconds;
        /*  //$interval angularjs提供的定时处理服务对象
         $scope.count=10;
         //参数一：定时器每隔多长时间做的事情  参数二：定时器时间设定 单位是毫秒值  参数三：设定执行次数
         $interval(function () {
             $scope.count--;
         }, 1000);*/

    }
    //秒杀下单
    $scope.saveSeckillOrder=function () {
        seckillService.saveSeckillOrder($scope.seckillGoodsId).success(function (response) {
            alert(response.message);
        })
    }
});	
