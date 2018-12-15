//服务层
app.service('uploadService',function($http){
	    	

	//基于angularjs结合H5实现文件上传
	this.uploadFile=function () {
		//创建H5的表单对象
		var formData = new FormData();
		//参数一:表单提交值的名称,与后端接受的参数名是一致的
		//参数二:要提交的文件对象 file.files[0] 第一个file指的是<input type="file" id="file"/> 标签的id值
		formData.append("file",file.files[0]);
		return $http({
			//提交方式
			method:"post",
			//提交路径
			url:"../upload/uploadFile.do",
			//数据就是formData
			data:formData,
			//浏览器会帮我们把 Content-Type 设置为 multipart/form-data.
            headers: {'Content-Type':undefined},
			//anjularjs transformRequest function 将序列化我们的formdata object.
            transformRequest: angular.identity
		})
    }

});
