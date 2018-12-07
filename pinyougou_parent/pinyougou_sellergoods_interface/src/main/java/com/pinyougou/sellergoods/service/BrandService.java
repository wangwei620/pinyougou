package com.pinyougou.sellergoods.service;

import com.pinyougou.entity.PageResult;
import com.pinyougou.pojo.TbBrand;

import java.util.List;
import java.util.Map;

public interface BrandService {

    //查询所有品牌
    public List<TbBrand> findAll();
    //添加
    void addBrand(TbBrand brand);
    //通过id查找
    TbBrand findOne(Long id);
    //修改品牌
    void updateBrand(TbBrand brand);
    //删除品牌
    void delete(Long[] ids);
    //条件查询+分页实现
    PageResult findPageByNameAndChar(TbBrand brand, int pageNum, int pageSize);
    //模板关联查询  品牌表的所有数据
    List<Map> selectBrandList();
}
