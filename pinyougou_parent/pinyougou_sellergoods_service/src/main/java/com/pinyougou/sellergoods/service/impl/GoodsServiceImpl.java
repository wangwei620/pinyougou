package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.entity.PageResult;
import com.pinyougou.groupentity.Goods;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbBrandMapper brandMapper;
    @Autowired
    private TbSellerMapper sellerMapper;
    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Goods goods) {
        //获取goods表,我们在插入的时候获取插入时的id，因为这个表关联goodsDesc
        TbGoods tbGoods = goods.getTbGoods();
        tbGoods.setAuditStatus("0");//初始录入时商品状态为0
        goodsMapper.insert(tbGoods);

        //获取goodsDesc表
        TbGoodsDesc tbGoodsDesc = goods.getTbGoodsDesc();
        tbGoodsDesc.setGoodsId(tbGoods.getId());
        goodsDescMapper.insert(tbGoodsDesc);
        //判断是否启用
        if ("1".equals(tbGoods.getIsEnableSpec())) {
            //启用规格
//组装tb_item表
            List<TbItem> items = goods.getItems();
            for (TbItem item : items) {
			/*后台组装
			  `title` varchar(100) NOT NULL COMMENT '商品标题',   // 商品名称（SPU名称）+ 商品规格选项名称 中间以空格隔开
			  `image` varchar(2000) DEFAULT NULL COMMENT '商品图片',  // 从 tb_goods_desc item_images中获取第一张
			  `categoryId` bigint(10) NOT NULL COMMENT '所属类目，叶子类目',  //三级分类id
			  `create_time` datetime NOT NULL COMMENT '创建时间',
			  `update_time` datetime NOT NULL COMMENT '更新时间',
			  `goods_id` bigint(20) DEFAULT NULL,
			  `seller_id` varchar(30) DEFAULT NULL,
					//以下字段作用：
			  `category` varchar(200) DEFAULT NULL, //三级分类名称
			  `brand` varchar(100) DEFAULT NULL,//品牌名称
			  `seller` varchar(200) DEFAULT NULL,//商家店铺名称*/
                String title = tbGoods.getGoodsName();
                //{"机身内存":"16G","网络":"联通3G"}
                String spec = item.getSpec();
                Map<String, String> specMap = JSON.parseObject(spec, Map.class);
                for (String s : specMap.keySet()) {
                    title += " " + specMap.get(s);
                }
                item.setTitle(title);
                setItemValue(tbGoods, tbGoodsDesc, item);

                itemMapper.insert(item);
            }

        } else {
            //为不启用规格
            TbItem item = new TbItem();
            item.setTitle(tbGoods.getGoodsName());
            setItemValue(tbGoods, tbGoodsDesc, item);
            /*为启用规格时，组装页面需要提交的数据
			`spec` varchar(200) DEFAULT NULL,
			 `price` decimal(20,2) NOT NULL COMMENT '商品价格，单位为：元',
			 `num` int(10) NOT NULL COMMENT '库存数量',
			 `status` varchar(1) NOT NULL COMMENT '商品状态，1-正常，2-下架，3-删除',
			 `is_default` varchar(1) DEFAULT NULL,*/
            item.setSpec("{}");
            item.setPrice(tbGoods.getPrice());
            item.setNum(9999);
            item.setStatus("1");
            item.setIsDefault("1");
            itemMapper.insert(item);
        }

    }
//抽取item工具的方法
    private void setItemValue(TbGoods tbGoods, TbGoodsDesc tbGoodsDesc, TbItem item) {
        //image数据组装
        String itemImages = tbGoodsDesc.getItemImages();
        List<Map> imageList = JSON.parseArray(itemImages, Map.class);
        //image数据组装
        //[{"color":"红色","url":"http://192.168.25.133/group1/M00/00/01/wKgZhVmHINKADo__AAjlKdWCzvg874.jpg"},
        // {"color":"黑色","url":"http://192.168.25.133/group1/M00/00/01/wKgZhVmHINyAQAXHAAgawLS1G5Y136.jpg"}]
        if (imageList != null && imageList.size() > 0) {
            String image = (String) imageList.get(0).get("url");
            item.setImage(image);
        }
        //三级分类id
        item.setCategoryid(tbGoods.getCategory1Id());
        //时间设置
        item.setCreateTime(new Date());
        item.setUpdateTime(new Date());
        //goods_id
        item.setGoodsId(tbGoods.getId());
//			selller_id
        item.setSellerId(tbGoods.getSellerId());
        //三级分类名称
        //注入三个mappper
        TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
        item.setCategory(tbItemCat.getName());
        TbBrand tbBrand = brandMapper.selectByPrimaryKey(tbGoods.getBrandId());
        item.setBrand(tbBrand.getName());
        TbSeller tbSeller = sellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
        item.setSeller(tbSeller.getNickName());
    }

    /**
     * 修改
     */
    @Override
    public void update(TbGoods goods) {
        goodsMapper.updateByPrimaryKey(goods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbGoods findOne(Long id) {
        return goodsMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            goodsMapper.deleteByPrimaryKey(id);
        }
    }

    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();

        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                criteria.andSellerIdEqualTo(goods.getSellerId() );
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }

        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

}
