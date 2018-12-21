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
    //完成门户网站的搜索与搜索模块的对接
    $scope.search=function () {
        //angularjs页面传参时候要在问号前面添加#    这个路由传参   ng ngroute
        location.href="http://search.pinyougou.com/search.html#?keywords="+$scope.keywords
    }
    
})