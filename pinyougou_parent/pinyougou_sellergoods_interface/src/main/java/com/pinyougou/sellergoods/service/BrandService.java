package com.pinyougou.sellergoods.service;

import com.pinyougou.entity.PageResult;
import com.pinyougou.pojo.TbBrand;

import java.util.List;

public interface BrandService {

    //查询所有品牌
    public List<TbBrand> findAll();
    //品牌的分页实现
    PageResult findPage(Integer pageNum, Integer pageSize);
    //添加
    void addBrand(TbBrand brand);
    //通过id查找
    TbBrand findOne(Long id);
    //修改品牌
    void updateBrand(TbBrand brand);
    //删除品牌
    void delete(Long[] ids);
    //条件查询
    PageResult findPageByNameAndChar(TbBrand brand, int pageNum, int pageSize);
}
