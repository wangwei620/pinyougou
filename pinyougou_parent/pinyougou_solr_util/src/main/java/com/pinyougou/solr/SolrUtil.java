package com.pinyougou.solr;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;


    public void dataImport(){
        //从数据库中查询满足条件的的商品列表数据 注意查询的是符合条件的:  上架的   状态为 1的
      List<TbItem> itemList = itemMapper.findAllGrounding();
        for (TbItem item : itemList) {
            String spec = item.getSpec();
             //组装动态域属性
            Map<String,String> map = JSON.parseObject(spec, Map.class);
            item.setSpecMap(map);
        }
        //2.将查询的结果导入到索引库
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
        System.out.println("itemList import finished.....");
    }

}
