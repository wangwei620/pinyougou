 //控制层 
app.controller('goodsController' ,function($scope,$controller ,typeTemplateService,uploadService  ,goodsService,itemCatService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.tbGoods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{

			//设置商家字段 通过kindEditer获取
			$scope.entity.tbGoodsDesc.introduction=editor.html();
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//清空商品数据
					$scope.entity={};
					//清空富文本编辑器
					editor.html("");
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//查询三级联动分类信息
	$scope.selectItemCatList=function () {
		itemCatService.findByParentId(0).success(function (response) {

			$scope.itemCat1List=response;
        })
    }
    //监控一级分类下的二级分类
	//参数一：监控的变量值，参数二：监控变化后，需要的做的事
	$scope.$watch("entity.tbGoods.category1Id",function (newValue,oldValue) {
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.itemCat2List=response;
        })
    })
    //监控二级分类下的三级分类
    //参数一：监控的变量值，参数二：监控变化后，需要的做的事
    $scope.$watch("entity.tbGoods.category2Id",function (newValue,oldValue) {
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.itemCat3List=response;
           //遗留的问题如何清理
        })
    })
    //监控三级分类下的模板id
    //参数一：监控的变量值，参数二：监控变化后，需要的做的事
    $scope.$watch("entity.tbGoods.category3Id",function (newValue,oldValue) {
        itemCatService.findOne(newValue).success(function (response) {
            $scope.entity.tbGoods.typeTemplateId=response.typeId;
            //遗留的问题如何清空三级的标题
        })
    })
    //监控模板下的查询关联数据
    //参数一：监控的变量值，参数二：监控变化后，需要的做的事
    $scope.$watch("entity.tbGoods.typeTemplateId",function (newValue,oldValue) {
        typeTemplateService.findOne(newValue).success(function (response) {
        	//注意一定要把品牌的json字符串变为json对象
            $scope.brandList=JSON.parse(response.brandIds);

            $scope.entity.tbGoodsDesc.customAttributeItems = JSON.parse(response.customAttributeItems);
        });
        //查询规格的规格列表数据
        typeTemplateService.findSpecList(newValue).success(function (response) {
            $scope.specList =response;
        })

    })
	//上传图片
	$scope.uploadFile=function () {
		uploadService.uploadFile().success(function (response) {
			//如果上传成功获得url
			if (response.success){
				//设置文件地址
				$scope.image_entity.url=response.message;
			}else{
				alert(response.message);
			}
        }).error(function () {
			alert("上传发生错误");
        })
    }
	//初始化entity
	$scope.entity={tbGoods:{},tbGoodsDesc:{itemImages:[],specificationItems:[]},items:[]}
	//添加上传图片到商品列表中
	$scope.addImageEntity=function () {
        $scope.entity.tbGoodsDesc.itemImages.push($scope.image_entity);
    }
    //从列表中删除
    $scope.deleImageEntity=function (index) {
        $scope.entity.tbGoodsDesc.itemImages.splice(index,1)
    }
    //组装商品录入勾选的的规格列表属性
	$scope.updateSpecAttribute=function ($event,specName,specOption) {
		//判断规格名称是否存在于勾选的列表中
        var specObject=  $scope.getObjectByKey($scope.entity.tbGoodsDesc.specificationItems,"attributeName",specName);
		if (specObject!=null){
			//如果存在
			//判断是勾选还是取消勾选规格选项
			if($event.target.checked){
				//勾选,在原有的规格选项数组中,添加勾选的规格选项名称
				specObject.attributeValue.push(specOption);
			}else{
				//取消勾选,在原有的规格选项数组中移除,取消勾选的规格选项名称
				var index  = specObject.attributeValue.indexOf(specOption);
				specObject.attributeValue.splice(index,1);
				//如果取消规格对应的所有规格选项,则从这个规格列表中移除该规格数据
				if(specObject.attributeValue.length<=0){
					var index1  = $scope.entity.tbGoodsDesc.specificationItems.indexOf(specObject);
					$scope.entity.tbGoodsDesc.specificationItems.splice(index1,1);
				}
			}

		}else{
			//如果不存在
			$scope.entity.tbGoodsDesc.specificationItems.push({"attributeName":specName,"attributeValue":[specObject]});
		}
    }
});	
