package com.pinyougou.page.service;

import com.pinyougou.groupentity.Goods;

public interface PageService {
    /**
     * 通过id查找Goods表的数据
     */
   public  Goods findOne(Long goodsId);
}
