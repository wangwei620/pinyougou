 //控制层 
app.controller('payController' ,function($scope,$controller ,$location  ,payService){
	
	$controller('baseController',{$scope:$scope});//继承

    //生成二维码
    $scope.createNative=function () {
        payService.createNative().success(function (response) {
            //接受后端传过来的支付订单号和支付金额
            $scope.out_trade_no=response.out_trade_no;
            $scope.total_fee=(response.total_fee)/100;

            //基于qrious生成二维码
            new QRious({
                element: document.getElementById('qrious'),
                size: 300,
                value: response.code_url,
                level:'H'
            });
            $scope.queryPayStatus();
        })
    }
    //查询支付状态
    $scope.queryPayStatus=function () {
        payService.queryPayStatus($scope.out_trade_no).success(function (response) {
            if(response.success){
                //支付成功
                location.href="paysuccess.html#?money="+$scope.total_fee;
            }else {
                if(response.message=="timeout"){
                    //支付超时
                    $scope.createNative();
                }
                //支付失败
                location.href="payfail.html";
            }
        })
    }
    //获取支付金额
    $scope.getMoney=function () {
        $scope.money=$location.search()["money"];
    }

});	
