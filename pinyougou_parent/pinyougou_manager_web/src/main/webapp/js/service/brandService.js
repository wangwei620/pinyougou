app.service("brandService",function ($http) {
    //查询所有
    this.findAll=function () {
        return $http.get("../brand/findAll.do");
    }
    //条件查询+分页

    this.search=function (searchEntity,pageNum,pageSize) {
        return $http.post("../brand/search.do?pageNum="+pageNum+"&pageSize="+pageSize,searchEntity);
    }

    //根据品牌查询
    this.findOne=function (id) {
        return $http.get("../brand/findOne.do?id="+id);
    }

    //删除品牌
    this.delete=function (selectIds) {
        return $http.get("../brand/delete.do?ids="+selectIds)
    }

    //添加
    this.add=function (entity) {
        return $http.post("../brand/add.do",entity);
    }

    //修改
    this.update=function (entity) {
        return $http.post("../brand/update.do",entity);
    }

    //查询模板关联   品牌下拉列表
    this.selectBrandList=function () {
        return $http.get("../brand/selectBrandList.do");
    }

})