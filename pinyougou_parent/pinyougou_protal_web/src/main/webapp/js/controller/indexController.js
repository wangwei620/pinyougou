app.controller("indexController",function ($scope,$controller,contentService) {

    //控制器继承代码
    $controller("baseController",{$scope:$scope});

    //根据广告id查询广告的列表数据
    $scope.findCategoryId=function (categoryId) {
        contentService.findCategoryId(categoryId).success(function (response) {
            //定义广告列表接受数据
            $scope.contentList = response;
        })
    }
    
})