 //控制层 
app.controller('orderController' ,function($scope,$controller   ,cartService,addressService,orderService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findCartList=function(){
        cartService.findCartList().success(
			function(response){
				$scope.cartList=response;
				sum();
			}			
		);
	}
    //展示收件人地址列表
    $scope.findAddressListByUserId=function () {
        addressService.findAddressListByUserId().success(function (response) {
            //收件人地址列表
            $scope.addressList=response;

            for(var i=0;i< $scope.addressList.length;i++){
                if($scope.addressList[i].isDefault=='1'){//默认收件人地址
                    $scope.address=$scope.addressList[i];
                    break;
                }
            }

            //如果没有设置默认收件人地址，取第一个地址为默认地址
            if($scope.address==null){
                $scope.address=$scope.addressList[0];
            }

        })
    }

    //定义寄送至的收件人地址对象
    $scope.address=null;

    //勾选默认收件人地址
    $scope.isSelected=function (addr) {
        if($scope.address==addr){
            return true;
        }else {
            return false;
        }
    }
    //跟改点击选中的状态然后我们,动态给address赋值
    $scope.updateSelected=function (addr) {
        $scope.address=addr;
    }
    //支付方式,
    //定义一个实体类
    $scope.entity={paymentType:"1"};
    $scope.updatePaymentType=function (type) {
        $scope.entity.paymentType=type;
    }

	//统计商品的数量和总计
	sum=function () {
		//总数量 和总金额
		$scope.totalNum = 0;
		$scope.totalMoney=0.00;
		//遍历购物车列表
		for (var i = 0;i<$scope.cartList.length;i++){
			//获取购物车对象
			var cart = $scope.cartList[i];
			var orderItemList = cart.orderItemList;//获取商品明细列表
			//遍历商品购物车明细列表
			for(var j = 0;j<orderItemList.length;j++){
				$scope.totalNum = orderItemList[i].num;
				$scope.totalMoney= orderItemList[i].totalFee;
			}
		}
    }
    //保存订单
    $scope.save=function () {
        $scope.entity.receiver=$scope.address.contact;//联系人
        $scope.entity.receiverAreaName=$scope.address.address;//联系人地址
        $scope.entity.receiverMobile=$scope.address.mobile;//联系电话

        orderService.add($scope.entity).success(function (response) {
            if(response.success){
                //跳转支付页面
                location.href="pay.html";
            }else {
                alert(response.message);
            }
        })
    }

});	
