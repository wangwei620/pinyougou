app.service("specificationService",function ($http) {
    //查询所有
    this.findAll=function () {
        return $http.get("../specification/findAll.do");
    }
    //条件查询+分页

    this.search=function (searchEntity,pageNum,pageSize) {
        return $http.post("../specification/search.do?pageNum="+pageNum+"&pageSize="+pageSize,searchEntity);
    }

    //根据品牌查询
    this.findOne=function (id) {
        return $http.get("../specification/findOne.do?id="+id);
    }

    //删除品牌
    this.delete=function (selectIds) {
        return $http.get("../specification/delete.do?ids="+selectIds)
    }

    //添加
    this.add=function (entity) {
        return $http.post("../specification/add.do",entity);
    }

    //修改
    this.update=function (entity) {
        return $http.post("../specification/update.do",entity);
    }

})