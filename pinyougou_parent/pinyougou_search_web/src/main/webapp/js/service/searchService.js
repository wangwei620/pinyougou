//服务层
app.service('searchService',function($http){

    //搜索功能的实现
    this.search=function(searchMap){
        return $http.post('itemsearch/search.do',searchMap);
    }
});
