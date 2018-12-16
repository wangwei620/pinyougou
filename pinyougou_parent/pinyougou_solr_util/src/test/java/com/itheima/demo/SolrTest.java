package com.itheima.demo;

import com.pinyougou.pojo.TbItem;
import com.pinyougou.solr.SolrUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext*.xml")
public class SolrTest {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private SolrUtil solrUtil;

    /**
     * 导入商品数据
     */
    @Test
    public void dataImport(){
        solrUtil.dataImport();
    }

    /**
     * 添加商品到商品库
     * 修改都是调用这个方法  saveBean方法
     */
    @Test
    public void addTest(){

        TbItem item = new TbItem();
        item.setId(1L);
        item.setTitle("苹果maxs   移动4G  64G");
        item.setSeller("苹果旗舰店");
        item.setBrand("苹果");
        solrTemplate.saveBean(item);
        //必须提交
        solrTemplate.commit();
    }

    /**
     * 通过id查询
     */
    @Test
    public void findByIdTest(){

        TbItem byId = solrTemplate.getById(1L, TbItem.class);
        System.out.println(byId.getId()+" " +byId.getTitle()+" "+byId.getBrand());
    }
    /**
     * 通过id删除
     */
    @Test
    public void deleteTest(){

        solrTemplate.deleteById("1");
        solrTemplate.commit();
    }
    /**
     * 删除所有数据
     */
    @Test
    public void deleteAllTest(){

        SolrDataQuery query = new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
    /**
     * 批量添加数据
     */
    @Test
    public void addAllTest(){
        ArrayList list = new ArrayList();
        for (long i = 1; i <= 100; i++) {
            TbItem item = new TbItem();
            item.setId(i);
            item.setTitle(i+"苹果maxs   移动4G  64G");
            item.setSeller("苹果"+i+"号旗舰店");
            item.setBrand("苹果");
            list.add(item);
            //solrTemplate.saveBean(item);
            //必须提交
           // solrTemplate.commit();
        }
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }
    /**
     * 分页查询
     */
    @Test
    public void findPageTest(){
        //设置查询对象
        Query query = new SimpleQuery("*:*");
        //设置分页条件
        query.setOffset(2);//设置分页查询起始值, 默认值 0 ,从第一天开始
        query.setRows(5);//每页查询记录数
        ScoredPage<TbItem> tbItems = solrTemplate.queryForPage(query, TbItem.class);
        System.out.println("总记录数:"+tbItems.getTotalElements());
        System.out.println("总页数"+tbItems.getTotalPages());
        //当前页数据列表
        List<TbItem> content = tbItems.getContent();
        for (TbItem item : content) {
            System.out.println(item.getId()+" "+item.getTitle()+" "+item.getBrand()+" "+item.getSeller());
        }
    }
    /**
     * 条件查询   需求查询  标题含有9    商家含有 5
     */
    @Test
    public void findMultilTest(){
        //设置查询对象   注意一定要有查询条件*:*
        Query query = new SimpleQuery("*:*");

        //构建查询条件
        //支持链式编程
        Criteria criteria = new Criteria("item_title").contains("9").and("item_seller").contains("5");
        //把构建好的查询对象赋值给查询对象
        query.addCriteria(criteria);
        ScoredPage<TbItem> tbItems = solrTemplate.queryForPage(query, TbItem.class);
        System.out.println("总记录数:"+tbItems.getTotalElements());
        System.out.println("总页数"+tbItems.getTotalPages());
        //当前页数据列表
        List<TbItem> content = tbItems.getContent();
        for (TbItem item : content) {
            System.out.println(item.getId()+" "+item.getTitle()+" "+item.getBrand()+" "+item.getSeller());
        }
    }


}
