package com.pinyougou.search.service;

import java.util.Map;

/**
 * 搜索接口的方法
 */
public interface SearchService {
    //商品的搜索参数   通过map格式,Object是不同的类型,包括品牌,规格,分类 价格区间 每页显示条数 每页记录数
    public Map<String,Object> search(Map searchMap);
}
