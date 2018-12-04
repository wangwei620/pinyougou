package com.pinyougou.mapper;

import com.pinyougou.pojo.TbBrand;

import java.util.List;

public interface TbBrandMapper {
    /**
     * 查询所有
     */
    public List<TbBrand> findAll();

    /**
     * 添加
     * @param brand
     */
    void addBrand(TbBrand brand);

    /**
     * 通过id找品牌
     * @param id
     * @return
     */
    TbBrand findOne(Long id);

    /**
     * 根据id修改品牌
     * @param brand
     */
    void updateBrand(TbBrand brand);

    /**
     * 删除
     * @param id
     */
    void delete(Long id);

    /**
     * 条件查询
     * @param brand
     * @return
     */
    List<TbBrand>  findByBrand(TbBrand brand);
}
