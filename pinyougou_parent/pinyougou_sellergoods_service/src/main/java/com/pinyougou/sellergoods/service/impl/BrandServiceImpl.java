package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.entity.PageResult;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    @Autowired
    private TbBrandMapper brandMapper;
    //查询所有
    @Override
    public List<TbBrand> findAll() {
        return brandMapper.selectByExample(null);
    }

    //添加品牌
    @Override
    public void addBrand(TbBrand brand) {
        brandMapper.insert(brand);
    }
    //通过id找品牌
    @Override
    public TbBrand findOne(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }
    //修改品牌
    @Override
    public void updateBrand(TbBrand brand) {
        brandMapper.updateByPrimaryKey(brand);
    }

    //删除
    @Override
    public void delete(Long[] ids) {
        //循环删除
        for (Long id : ids) {
            brandMapper.deleteByPrimaryKey(id);
        }
    }
    //条件查询+分页实现
    @Override
    public PageResult findPageByNameAndChar(TbBrand brand, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        //设置条件
        TbBrandExample example = new TbBrandExample();
        TbBrandExample.Criteria criteria = example.createCriteria();
        if (brand!=null){
            String brandName = brand.getName();
            if (brandName!=null && !"".equals(brandName)){
                //模糊查询
                criteria.andNameLike(brandName);
            }
            String brandFirstChar = brand.getFirstChar();
            if (brandFirstChar!=null&&!"".equals(brandFirstChar)){
                //等值查询
                criteria.andFirstCharEqualTo(brandFirstChar);
            }

        }
        Page pageResult = (Page)  brandMapper.selectByExample(example);
        return new PageResult(pageResult.getTotal(),pageResult.getResult());
    }
}
