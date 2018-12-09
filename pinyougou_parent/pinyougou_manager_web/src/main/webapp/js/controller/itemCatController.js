 //控制层 
app.controller('itemCatController' ,function($scope,$controller   ,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); //修改  
		}else{
			//新增分类是;指定该跟类的父id
			$scope.entity.parentId=$scope.parentId;
			serviceObject=itemCatService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.findByParentId($scope.parentId);//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){
	    if(confirm("确定要删除吗?")){
            //获取选中的复选框
            itemCatService.dele( $scope.selectIds ).success(
                function(response){
                    if(response.success){
                        $scope.findByParentId($scope.parentId);//刷新列表
                    }
                }
            );
        }

	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	//记录当前分类的父id
	$scope.parentId=0;
	// 根据父id找子类
    $scope.findByParentId=function(parentId){
    	//为父id赋值
		$scope.parentId=parentId;
        itemCatService.findByParentId(parentId).success(
            function(response){
                $scope.list= response;
            }
        );
    }
    //定义级别  注意我们面包屑的存储数据结构是{id:"name"} key:value的形式
	//首先定义一个grade=1
	$scope.grade=1;
	//对级别进行 赋值    通过传过来的grade值
	$scope.setGrade=function (grade) {
		$scope.grade=grade;
    }
    //对面包屑导航栏的级别和grade有关
	//entity_p  为父分类的对象
	$scope.selectCatList=function (entity_p) {
		//如果是一级分类
		if ($scope.grade==1){
			$scope.entity_1=null;
			$scope.entity_2=null;
		}
        //如果是二级分类
        if ($scope.grade==2){
            $scope.entity_1=entity_p;
            $scope.entity_2=null;
        }
        //如果是一级分类
        if ($scope.grade==3){
            $scope.entity_2=entity_p;
        }
        //调用父id查询子分类
        $scope.findByParentId(entity_p.id);
    }

    //查找分类模板的关联的模板数据
	$scope.findTemplateList=function () {
		typeTemplateService.findAll().success(function (response) {
			$scope.templateList=response;
        })
    }
});	
