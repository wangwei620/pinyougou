app.controller("indexController",function ($scope,$controller,loginService) {

    //控制器继承代码  参数一：继承的父控制器名称  参数二：固定写法，共享$scope对象
    $controller("baseController",{$scope:$scope});

    //定义查询所有品牌列表的方法
    $scope.getName=function () {
        //response接受相应结果
        loginService.getName().success(function (response) {
            $scope.loginName = response.loginName;
        })
    }
})