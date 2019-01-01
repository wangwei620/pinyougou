 //控制层 
app.controller('cartController' ,function($scope,$controller   ,cartService){
	
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

	//添加商品到购物车
	$scope.addItemToCartList=function (itemId,num) {
		cartService.addItemToCartList(itemId,num).success(function (response) {
			if (response.success){
				//添加购物车工程
                $scope.findCartList();
			}else{
				//添加购物车失败
				alert(response.message)
			}
        })
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

});	
