app.controller("searchController",function ($scope,$controller,$location,searchService) {

    //控制器继承代码
    $controller("baseController",{$scope:$scope});

    //构建封装搜索条件的对象
    $scope.searchMap={
      keywords:"",
        category:"",
        brand:"",
        spec:{},//规格属性,需要规格名称和规格选项
        price:"",
        sort:"ASC",
        sortField:"",
        pageNo:1,//当前页
        pageSize:60//每页记录数
    };

    //基于$location接受门户网站传过来的搜索关键字,$location.search()  获取的值为Object对象
   var keywords =  $location.search()["keywords"];
   //判断是否是空值传过来的,空值的话是"undefined"字符串
    if (keywords!="undefined"){
        //输入的搜索的关键字
        $scope.searchMap.keywords=keywords;
    }else{
        //反之没有输入关键字
        $scope.searchMap.keywords="手机";//给一个默认值作为搜索的关键字
    }
    //商品搜索
    $scope.search=function () {
        searchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap=response;
            //构建分页工具条
            buildPageLabel();
        })
    }
    //组合条件的查询
    $scope.addFilterCondition=function (key,value) {
        if (key=="category" || key=="price" || key=="brand"){
            $scope.searchMap[key]=value;
        }else{
            $scope.searchMap.spec[key]=value;
        }
        //调用查询方法
        $scope.search();
    }
    //移除查询条件操作  //通过key移除值
    $scope.removeSearchItem=function (key) {

        if (key=="category" || key=="price" || key=="brand"){
            $scope.searchMap[key]="";
        }else{
          delete  $scope.searchMap.spec[key];
        }
        //调用查询方法
        $scope.search();
    }
    //排序的条件实现
    $scope.sortSearch=function (sortField,sort) {
        $scope.searchMap.sort=sort;//排序方式
        $scope.searchMap.sortField=sortField;//排序字段

        //调用查询方法
        $scope.search();
    }
    //构建分页工具条代码
    buildPageLabel=function(){
        $scope.pageLabel = [];// 新增分页栏属性，存放分页的页面
        var maxPageNo = $scope.resultMap.totalPages;// 得到最后页码

        // 定义属性,显示省略号
        $scope.firstDot = true;
        $scope.lastDot = true;

        var firstPage = 1;// 开始页码
        var lastPage = maxPageNo;// 截止页码

        if ($scope.resultMap.totalPages > 5) { // 如果总页数大于5页,显示部分页码
            if ($scope.resultMap.pageNo <= 3) {// 如果当前页小于等于3
                lastPage = 5; // 前5页
                // 前面没有省略号
                $scope.firstDot = false;

            } else if ($scope.searchMap.pageNo >= lastPage - 2) {// 如果当前页大于等于最大页码-2
                firstPage = maxPageNo - 4; // 后5页
                // 后面没有省略号
                $scope.lastDot = false;
            } else {// 显示当前页为中心的5页
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
            }
        } else {
            // 页码数小于5页  前后都没有省略号
            $scope.firstDot = false;
            $scope.lastDot = false;
        }
        // 循环产生页码标签
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
        }
    }


    //分页查询
    $scope.queryForPage=function(pageNo){
        $scope.searchMap.pageNo=pageNo;

        //执行查询操作
        $scope.search();

    }

    //分页页码显示逻辑分析：
    // 1,如果页面数不足5页,展示所有页号
    // 2,如果页码数大于5页
    // 1) 如果展示最前面的5页,后面必须有省略号.....
    // 2) 如果展示是后5页,前面必须有省略号
    // 3) 如果展示是中间5页,前后都有省略号

    // 定义函数,判断是否是第一页
    $scope.isTopPage = function() {
        if ($scope.searchMap.pageNo == 1) {
            return true;
        } else {
            return false;
        }
    }
    // 定义函数,判断是否最后一页
    $scope.isLastPage = function() {
        if ($scope.searchMap.pageNo == $scope.resultMap.totalPages) {
            return true;
        } else {
            return false;
        }
    }


})