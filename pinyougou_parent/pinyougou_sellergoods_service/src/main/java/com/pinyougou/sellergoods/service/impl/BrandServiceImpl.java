package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.entity.PageResult;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    @Autowired
    private TbBrandMapper brandMapper;
    @Override
    public List<TbBrand> findAll() {
        return brandMapper.findAll();
    }

    //添加品牌
    @Override
    public void addBrand(TbBrand brand) {
        brandMapper.addBrand(brand);
    }
    //通过id找品牌
    @Override
    public TbBrand findOne(Long id) {
        return brandMapper.findOne(id);
    }
    //修改品牌
    @Override
    public void updateBrand(TbBrand brand) {
        brandMapper.updateBrand(brand);
    }

    //删除
    @Override
    public void delete(Long[] ids) {
        //循环删除
        for (Long id : ids) {
            brandMapper.delete(id);
        }
    }
    //条件查询+分页实现
    @Override
    public PageResult findPageByNameAndChar(TbBrand brand, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        Page pageResult = (Page)  brandMapper.findByBrand(brand);
        return new PageResult(pageResult.getTotal(),pageResult.getResult());
    }
}
