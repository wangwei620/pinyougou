app.controller("baseController",function($scope){
	// 重新加载列表 数据
	$scope.reloadList = function() {
		// 切换页码
		$scope.search($scope.paginationConf.currentPage,
				$scope.paginationConf.itemsPerPage);
	}
	// 分页控件配置
	$scope.paginationConf = {
		currentPage : 1,
		totalItems : 10,
		itemsPerPage : 10,
		perPageOptions : [ 10, 20, 30, 40, 50 ],
		onChange : function() {
			$scope.reloadList();// 重新加载
		}
	};

	// 初始化一个id数组
	$scope.selectIds = [];
	// [1,2,3]
	// 定义选择品牌列表id事件
	$scope.updateSelection = function($event, id) {
		// 判断品牌是否被选中
		if ($event.target.checked) {
			$scope.selectIds.push(id);
		} else {
			// 删除id
			$scope.selectIds.splice($scope.selectIds.indexOf(id), 1);
		}

	};
	////[{"id":33,"text":"电视屏幕尺寸"}]
	//把json数据转换成使用逗号分割数据格式
	$scope.jsonToString = function(jsonList,key){
		
		//定义组装空字符串
		var value = "";
		
		//把json字符串转换成对象
		var jsonObj = JSON.parse(jsonList);
		//循环数组对象
		for(var i=0;i<jsonObj.length;i++){
			//判断
			if(i>0){
				value += ",";
			}
			//组装数据
			value+=jsonObj[i][key];
		}
		
		return value;
	};
	//基于数组中对象的值  获取该对象返回
    //[{"attributeName":"网络","attributeValue":["移动3G","移动4G"]}]
	//list是传入的列表数组   key是attributeName  是根据传入值决定的    value 是对应的值,比如网络
	$scope.getObjectByKey = function (list,key,value) {
		for (var i=0;i<list.length;i++){
			//存在对象时
			if (list[i][key]==value){
				return list[i];
			}
		}
		//不存在则返回空值
		return null;
    }

});
