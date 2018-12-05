package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.entity.PageResult;
import com.pinyougou.groupentity.Specification;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.sellergoods.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {
    @Autowired
    TbSpecificationMapper specificationMapper;
    @Autowired
    TbSpecificationOptionMapper specificationOptionMapper;
    //分页并+搜索
    @Override
    public PageResult findPageByNameAndChar(TbSpecification specification, Integer pageNum, Integer pageSize) {
        //分页开始
        PageHelper.startPage(pageNum,pageSize);

        TbSpecificationExample example = new TbSpecificationExample();
        TbSpecificationExample.Criteria criteria = example.createCriteria();
        //判断
        if(specification!=null){
            String specName = specification.getSpecName();
            if (specName!=null&&!"".equals(specName)){
                criteria.andSpecNameLike(specName);
            }
        }
        Page pageResult = (Page) specificationMapper.selectByExample(example);

        return new PageResult(pageResult.getTotal(),pageResult.getResult());
    }

    //添加规格
    @Override
    public void add(Specification specification) {
        TbSpecification spe = specification.getSpecification();
        //插入tb_specefication表中
        specificationMapper.insert(spe);
        List<TbSpecificationOption> specificationOptions = specification.getSpecificationOptions();
        //循环添加
        for (TbSpecificationOption specificationOption : specificationOptions) {
            //关联id
            specificationOption.setSpecId(spe.getId());
            specificationOptionMapper.insert(specificationOption);
        }
    }

    @Override
    public Specification findOne(Long id) {
        //首先我们获取组合实体类
        Specification specification = new Specification();
        TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
        specification.setSpecification(tbSpecification);

        //查询规格表的设计
        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        criteria.andSpecIdEqualTo(id);
        List<TbSpecificationOption> tbSpecificationOptions = specificationOptionMapper.selectByExample(example);
        specification.setSpecificationOptions(tbSpecificationOptions);
        return specification;
    }

    @Override
    public void update(Specification specification) {
        //修改规格数据
        TbSpecification tbSpecification = specification.getSpecification();
        specificationMapper.updateByPrimaryKey(tbSpecification);

        //跟新选项列表,先删除,后根据前台提交的,在跟新提交的数据

        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        criteria.andSpecIdEqualTo(tbSpecification.getId());
        specificationOptionMapper.deleteByExample(example);

        List<TbSpecificationOption> specificationOptions = specification.getSpecificationOptions();
        for (TbSpecificationOption specificationOption : specificationOptions) {
            //关联规格
            specificationOption.setSpecId(tbSpecification.getId());
            specificationOptionMapper.insert(specificationOption);
        }
    }
    //删除方法
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            //根据id删除
            specificationMapper.deleteByPrimaryKey(id);
            //删除规格选项
            TbSpecificationOptionExample example = new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
            criteria.andSpecIdEqualTo(id);
            specificationOptionMapper.deleteByExample(example);
        }
    }
}
