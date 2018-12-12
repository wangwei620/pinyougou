package com.pinyougou.shop.controller;

import com.pinyougou.pojo.Result;
import com.pinyougou.util.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UploadController {
    /**
     * 文件上传controller
     */
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;//文件服务器的地址

    @RequestMapping("/uploadFile")
    public Result uploadFile(MultipartFile file){

        //1.获取文件的扩展名
        String originalFilename = file.getOriginalFilename();
        //2.通过字符串截取的方法进行获得
        String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        //3.创建一个fastDFS的客户端
        try {
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            //4.执行上传的处理
            String path = fastDFSClient.uploadFile(file.getBytes(), extName);
            //5.拼接返回的url和ip地址,拼接成完整的url
            String url = FILE_SERVER_URL+path;
            //6.返回结果数据
            return new Result(true,url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }
    }

}
