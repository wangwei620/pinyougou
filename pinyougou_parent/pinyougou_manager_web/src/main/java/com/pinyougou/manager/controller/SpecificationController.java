package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.entity.PageResult;
import com.pinyougou.groupentity.Specification;
import com.pinyougou.pojo.Result;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.sellergoods.service.SpecificationService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/specification")
@RestController//这个注解是Controller和ResponseBody的结合
public class SpecificationController {

    @Reference
    private SpecificationService specificationService;


   //条件查询+分页实现
    @RequestMapping("/search")
    public PageResult findPageBySpecification(@RequestBody TbSpecification specification, Integer pageNum, Integer pageSize){
        return specificationService.findPageByNameAndChar(specification,pageNum,pageSize);
    }
    /**
     * 添加
     */
    @RequestMapping("/add")
    public Result addBrand(@RequestBody Specification specification){
        try {
            specificationService.add(specification);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加成功");
        }
    }

     //通过id查找信息

    @RequestMapping("/findOne")
    public Specification findOne(Long id){
        return specificationService.findOne(id);
    }

     //修改brand

    @RequestMapping("/update")
    public Result update(@RequestBody Specification specification){
        try {
            specificationService.update(specification);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改成功");
        }
    }
     //批量删除

    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            specificationService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除成功");
        }
    }
}
