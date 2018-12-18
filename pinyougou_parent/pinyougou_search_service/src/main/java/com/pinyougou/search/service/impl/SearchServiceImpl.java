package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索功能的实现类
 */
@Service
@Transactional
public class SearchServiceImpl implements SearchService {

    @Autowired
    private SolrTemplate solrTemplate;//注意添加solr的配置文件
    @Override
    public Map<String, Object> search(Map searchMap) {
        //创建高亮显示对象
        HighlightQuery query = new SimpleHighlightQuery();//接口我们通过其子类实现即可
 // 一.  1.关键字搜索
        String keywords = (String) searchMap.get("keywords");
        Criteria criteria = null;
        //2.判断关键字是否为空
        if (keywords!=null&&!"".equals(keywords)){
            //不等于空
            //输入关键字条件条件
            criteria = new Criteria("item_keywords").is(keywords);
        }else{
            //关键字为空,查询所有 通过一个表达式  *:*
            criteria = new Criteria().expression("*:*");
        }

        //3.将查询的条件添加到criteria中
        query.addCriteria(criteria);
        //三.1.构建分类的查询条件
        String category = (String) searchMap.get("category");
        //2.判断查询条件是否为空
        if (category!=null&&!"".equals(category)){
            //构建分类查询条件
            Criteria categoryCriteria = new Criteria("item_category").is(category);
            //构建过滤条件查询
            FilterQuery filterQuery = new SimpleFilterQuery(categoryCriteria);
            //将查询的条件添加到query 中
            query.addFilterQuery(filterQuery);
        }
        //四.构建品牌查询条件
        String brand = (String)searchMap.get("brand");
        //判断是否为空
        if (brand!=null&&!"".equals(brand)){
            //构建品牌查询提哦案件
            Criteria brandCriteria = new Criteria("item_brand").is(brand);
            //构建过滤条件查询
            FilterQuery filterQuery  = new SimpleFilterQuery(brandCriteria);
            //将查询的条件添加到总的query 中
            query.addFilterQuery(filterQuery);
        }
        //五.  封装规格
        //获取spec的对象格式
        Map<String,String> specMap = (Map<String, String>) searchMap.get("spec");
        //判断map的结果是否Wie空
        if (specMap!=null){
            //通过遍历它的key值,获得value值,就是规格选项的值
            for (String key: specMap.keySet()){
                //构建品牌查询提哦案件
                Criteria specificationCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
                //构建过滤条件查询
                FilterQuery filterQuery  = new SimpleFilterQuery(specificationCriteria);
                //将查询的条件添加到总的query 中
                query.addFilterQuery(filterQuery);
            }
        }
        //六.价格的条件查询
        String price = (String)searchMap.get("price");
        //分析:
        /*
        ('price','0-500')
        ('price','500-1000')
        ('price','1000-1500')
        ('price','2000-3000')
          ('price','3000-*')分析可知通过分隔符分开,然后分割后成为一个数组,最后
          通过临界值判断
         */
        //判断是否为空
        if (price!=null&&!"".equals(price)){
            String[] split = price.split("-");
            //通过价格临界值判断
            if (!"0".equals(split[0])){
                //构建查询条件  大于0这个区间
                Criteria priceCriteria= new Criteria("item_price").greaterThanEqual(split[0]);
                //构建过滤条件查询
                FilterQuery filterQuery  = new SimpleFilterQuery(priceCriteria);
                //将查询的条件添加到总的query 中
                query.addFilterQuery(filterQuery);
            }

            //通过* 判断  小于等于1的这个区间
            if (!"*".equals(split[1])){
                //构建查询条件
                Criteria priceCriteria= new Criteria("item_price").lessThanEqual(split[1]);
                //构建过滤条件查询
                FilterQuery filterQuery  = new SimpleFilterQuery(priceCriteria);
                //将查询的条件添加到总的query 中
                query.addFilterQuery(filterQuery);
            }
        }

        //七.构建排序查询 根据传过来的字段进行处理
        String sortField = (String) searchMap.get("sortField");
        String sort = (String) searchMap.get("sort");
        if (sortField!=null&&!"".equals(sortField)){
            //构建排序操作
            if ("ASC".equals(sort)){//升序操作
                query.addSort(new Sort(Sort.Direction.ASC,"item_"+sortField));
            }else{
                query.addSort(new Sort(Sort.Direction.DESC,"item_"+sortField));
            }
        }
        //八.构建分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");
        Integer pageSize = (Integer) searchMap.get("pageSize");
        query.setOffset((pageNo-1)*pageSize);//分页起始值  0  60
        query.setRows(pageSize);//每页记录数
        //二.1.设置高亮
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");//设置高亮字段
        highlightOptions.setSimplePrefix("<font color='red'>");//设置前缀
        highlightOptions.setSimplePostfix("</font>");///设置后缀
        query.setHighlightOptions(highlightOptions);
        //4.这个条件已经封装了,query需要查询的所有条件
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //当前页的信息
        List<TbItem>  content = page.getContent();
        for (TbItem item : content) {
            //处理高亮结果
            List<HighlightEntry.Highlight> highlights = page.getHighlights(item);
            //判断highlightsshif为空
            if (highlights!=null&& highlights.size()>0){
                //获取高亮结果集
                List<String> snipplets = highlights.get(0).getSnipplets();
                //在判断是否为空
                if (snipplets!=null&& snipplets.size()>0){
                    item.setTitle(snipplets.get(0));
                }
            }
        }
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("rows",content);
        resultMap.put("totalPages",page.getTotalPages());//总页数
        resultMap.put("pageNo",pageNo);//当前页
        return resultMap;
    }
}
