app.controller("brandController",function ($scope,$controller,brandService) {

    //控制器继承代码  参数一：继承的父控制器名称  参数二：固定写法，共享$scope对象
    $controller("baseController",{$scope:$scope});

    //定义查询所有品牌列表的方法
    $scope.findAll=function () {
        //response接受相应结果
        brandService.findAll().success(function (response) {
            $scope.list = response;
        })
    }

    //条件查询
    $scope.searchEntity={};//解决初始化参数为空的情况
    $scope.search=function (pageNum,pageSize) {
        brandService.search($scope.searchEntity,pageNum,pageSize).success(function (response) {
            $scope.list = response.rows;
            $scope.paginationConf.totalItems=response.total;
        })
    }
    //添加品牌
    $scope.save=function () {
        var method=null;
        if($scope.entity.id!=null){
            //修改
            method=brandService.update($scope.entity)
        }else{
            //添加
            method=brandService.add($scope.entity)
        }
        method.success(function (response) {
            if(response.success){
                //添加成功
                $scope.reloadList();
            }else{
                alert(response.message);
            }
        })
    }
    //根据id查询品牌数据
    $scope.findOne=function (id) {
        brandService.findOne(id).success(function (response) {
            $scope.entity=response;
        })
    }

    //删除操作$http.get("../brand/delete.do?ids="+$scope.selectIds)
    $scope.delete=function () {
        if(confirm("您确定要删除吗")){
            brandService.delete($scope.selectIds).success(function (response) {
                if (response.success){
                    //删除成功,从新加载列表
                    $scope.reloadList();
                }else{
                    alert(response.message);
                }
            })
        }
    }


})