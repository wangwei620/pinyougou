package com.pinyougou.page.listener;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.io.File;
import java.util.List;

@Service
@Transactional
public class DeleItemPageMessageListener implements MessageListener {
    @Autowired
    private TbItemMapper itemMapper;
    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage)message;
        try {
            //获得商品id
            String goodsId = textMessage.getText();
            //在这我们通过生成静态页面的id序号删除页面
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(Long.parseLong(goodsId));
            //获得item表的信息
            List<TbItem> itemList = itemMapper.selectByExample(example);
            //循环删除
            for (TbItem item : itemList) {
                new File("F:\\item\\"+item.getId()+".html").delete();
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
