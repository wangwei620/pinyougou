app.controller("specificationController",function ($scope,$controller,specificationService) {

    //控制器继承代码  参数一：继承的父控制器名称  参数二：固定写法，共享$scope对象
    $controller("baseController",{$scope:$scope});

    //定义查询所有品牌列表的方法
    $scope.findAll=function () {
        //response接受相应结果
        specificationService.findAll().success(function (response) {
            $scope.list = response;
        })
    }

    //条件查询
    $scope.searchEntity={};//解决初始化参数为空的情况
    $scope.search=function (pageNum,pageSize) {
        specificationService.search($scope.searchEntity,pageNum,pageSize).success(function (response) {
            $scope.list = response.rows;
            $scope.paginationConf.totalItems=response.total;
        })
    }
    //添加规格
    $scope.save=function () {
        var method=null;
        if($scope.entity.specification.id!=null){
            //修改
            method=specificationService.update($scope.entity)
        }else{
            //添加
            method=specificationService.add($scope.entity)
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
        specificationService.findOne(id).success(function (response) {
            $scope.entity=response;
        })
    }

    //删除操作$http.get("../brand/delete.do?ids="+$scope.selectIds)
    $scope.delete=function () {
        if(confirm("您确定要删除吗")){
            specificationService.delete($scope.selectIds).success(function (response) {
                if (response.success){
                    //删除成功,从新加载列表
                    $scope.reloadList();
                }else{
                    alert(response.message);
                }
            })
        }
    }
    //新增规格选项行
    //注意在这有一个问题,记得初始化specificationOptions
    //初始化entity对象
    $scope.entity={specificationOptions:[]}
    $scope.addRow=function () {
        $scope.entity.specificationOptions.push({});
    }
    //删除规格
    $scope.deleRow=function (index) {
        $scope.entity.specificationOptions.splice(index,1);
    }

})