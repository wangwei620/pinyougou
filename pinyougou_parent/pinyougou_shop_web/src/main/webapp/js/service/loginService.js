app.service("loginService",function ($http) {
    //查询所有
    this.getName=function () {
        return $http.get("../login/getName.do");
    }

})