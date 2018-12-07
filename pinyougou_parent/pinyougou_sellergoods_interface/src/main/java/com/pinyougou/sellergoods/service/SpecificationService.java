package com.pinyougou.sellergoods.service;

import com.pinyougou.entity.PageResult;
import com.pinyougou.groupentity.Specification;
import com.pinyougou.pojo.TbSpecification;

import java.util.List;
import java.util.Map;

public interface SpecificationService {
    //规格的分页+条件
    PageResult findPageByNameAndChar(TbSpecification specification, Integer pageNum, Integer pageSize);
    //添加规格
    void add(Specification specification);
    //根据规格id查找
    Specification findOne(Long id);

    void update(Specification specification);

    void delete(Long[] ids);
    //模板关联  查询规格表
    List<Map> selectSpecList();
}
