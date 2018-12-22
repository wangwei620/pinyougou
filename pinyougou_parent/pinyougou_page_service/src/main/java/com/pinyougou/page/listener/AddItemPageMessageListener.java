package com.pinyougou.page.listener;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.groupentity.Goods;
import com.pinyougou.page.service.PageService;
import com.pinyougou.pojo.TbItem;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
@Transactional
public class AddItemPageMessageListener implements MessageListener {
    @Autowired
    private PageService pageService;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage)message;
        try {
            String goodsId = textMessage.getText();

            //生成静态页面
           // 第一步：创建一个 Configuration 对象，直接 new 一个对象。构造方法的参数就是
            freemarker.template.Configuration configuration = freeMarkerConfigurer.getConfiguration();
//        freemarker 的版本号。
//        第四步：加载一个模板，创建一个模板对象。
            Template template = configuration.getTemplate("item.ftl");
//        第五步：创建一个模板使用的数据集，可以是 pojo 也可以是 map。一般是 Map。
            Goods goods = pageService.findOne(Long.parseLong(goodsId));
            List<TbItem> items = goods.getItemList();
            for (TbItem item : items) {
                //      在这呢我们不要将组合实体类返回,因为返回还的遍历,取值,我们直接在这封装给map就可以了
                Map<String, Object> map = new HashMap<>();
                map.put("goods", goods);
                map.put("item", item);
//        第六步：创建一个 Writer 对象，一般创建一 FileWriter 对象，指定生成的文件名。
                Writer out = new FileWriter("F:\\item\\" + item.getId() + ".html");
//        第七步：调用模板对象的 process 方法输出文件。
                template.process(map, out);
//        第八步：关闭流
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
