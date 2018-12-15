//服务层
app.service('contentService',function($http){

    //根据id查询广告的分类
    this.findCategoryId=function(categoryId){
        return $http.get('content/findCategoryId.do?categoryId='+categoryId);
    }
});
