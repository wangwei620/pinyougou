package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.entity.PageResult;
import com.pinyougou.pojo.Result;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/brand")
@RestController//这个注解是Controller和ResponseBody的结合
public class BrandController {

    @Reference
    private BrandService brandService;

    @RequestMapping("findAll")
    public List<TbBrand> findAll(){

        return brandService.findAll();
    }
    /**
     * 品牌的分页数据查询
     */
   /* @RequestMapping("/findPage")
    public PageResult findPage(Integer pageNum, Integer pageSize){
        return  brandService.findPage(pageNum,pageSize);
    }*/
    @RequestMapping("/search")
    public PageResult findPageByNameAndChar(@RequestBody TbBrand brand,Integer pageNum, Integer pageSize){
        return brandService.findPageByNameAndChar(brand,pageNum,pageSize);
    }
    /**
     * 添加
     */
    @RequestMapping("/addBrand")
    public Result addBrand(@RequestBody TbBrand brand){
        try {
            brandService.addBrand(brand);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加成功");
        }
    }
    /**
     * 通过id查找信息
     */
    @RequestMapping("/findOne")
    public TbBrand findOne(Long id){
        return brandService.findOne(id);
    }
    /**
     * 修改brand
     */
    @RequestMapping("/updata")
    public Result updateBrand(@RequestBody TbBrand brand){
        try {
            brandService.updateBrand(brand);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改成功");
        }
    }
    /**
     * 批量删除
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            brandService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除成功");
        }
    }
    /*@RequestMapping("/search")
    public PageResult findPageByNameAndChar(@RequestBody TbBrand brand,int pageNum,int pageSize){
        return brandService.findPageByNameAndChar(brand,pageNum,pageSize);
    }*/
}
