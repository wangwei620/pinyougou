package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.entity.PageResult;
import com.pinyougou.groupentity.Cart;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
    //注入idWorker  id生成器
    @Autowired
    private IdWorker idWorker;
    @Autowired
	private TbPayLogMapper payLogMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}
    @Autowired
    private RedisTemplate redisTemplate;
	@Autowired
    private TbOrderItemMapper tbOrderItemMapper;
	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		//我们必须自己组装数据,很多的数据我们只能从购物车列表中获得,可能购物车中有两个商家,那么我么
		//要通过循环购物车列表来生成订单 ,从redis中获得购物车数据
        //订单与商家关联，订单数据有很多是来自购物车列表数据
        List<Cart> cartList = (List<Cart>) redisTemplate.boundValueOps(order.getUserId()).get();
        //循环购物车   ,组装订单  ,每个购物车列表就是一个订单
		//定义支付总金额
		double totalMoney = 0.00;
		//创建一个订单集合名称
		List<String> ids = new ArrayList<>();
        for (Cart cart : cartList) {
            //构建订单对象
            TbOrder tbOrder = new TbOrder();
            /*
            `order_id` bigint(20) NOT NULL COMMENT '订单id',  //不是主键自增
		 `payment` decimal(20,2) DEFAULT NULL COMMENT '实付金额。精确到2位小数;单位:元。如:200.07，表示:200元7分',
		 `status` varchar(1) COLLATE utf8_bin DEFAULT NULL COMMENT '状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价',
		 `create_time` datetime DEFAULT NULL COMMENT '订单创建时间',
		 `update_time` datetime DEFAULT NULL COMMENT '订单更新时间',
		 `user_id` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '用户id',
		 `source_type` varchar(1) COLLATE utf8_bin DEFAULT NULL COMMENT '订单来源：1:app端，2：pc端，3：M端，4：微信端，5：手机qq端',
		 `seller_id` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '商家ID',  //来着购物车
             */

            //订单id的封装
            long orderId = idWorker.nextId();
            ids.add(orderId+"");
            tbOrder.setOrderId(orderId);
            //订单状态封装
            tbOrder.setStatus("1");
            //创建订单时间
            tbOrder.setCreateTime(new Date());
            //创建跟新订单时间
            tbOrder.setUpdateTime(new Date());
            //用户id,因为我们在controller层已经封装 了,所以我们自己获得就行
            tbOrder.setUserId(order.getUserId());
            //订单来源
            tbOrder.setSourceType("2");
            //商家id
            tbOrder.setSellerId(cart.getSellerId());
            /*
            页面提交数据
	 `payment_type` varchar(1) COLLATE utf8_bin DEFAULT NULL COMMENT '支付类型，1、在线支付，2、货到付款',
	  `receiver_area_name` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人地区名称(省，市，县)街道',
	  `receiver_mobile` varchar(12) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人手机',
	  `receiver` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人',
             */
            //支付类型的封装
            tbOrder.setPaymentType(order.getPaymentType());
            //收货人地址
            tbOrder.setReceiverAreaName(order.getReceiverAreaName());
            //收货人手机
            tbOrder.setReceiverMobile(order.getReceiverMobile());
            //收货人
            tbOrder.setReceiver(order.getReceiver());

            //遍历购物车明细数据,组装订单详情数据
            //我们主要后台组装数据   连个数据  其他的度,在购物车添加数据的时候已经组装好了
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            //定义一个费用统计的变量
            double payment = 0.00;
            for (TbOrderItem orderItem : orderItemList) {
                //  `id` bigint(20) NOT NULL,    //不是主键自增
              	    orderItem.setId(idWorker.nextId());
                //		  `order_id` bigint(20) NOT NULL COMMENT '订单id',
                    orderItem.setOrderId(orderId);
                    //获得费用
                    payment+=orderItem.getTotalFee().doubleValue();
                    //保存到orderItem中
                tbOrderItemMapper.insert(orderItem);

            }
            //支付的总金额
            totalMoney+=payment;
            //payment  实付金额 我们不是前天传的,我们从后台算的
            tbOrder.setPayment(new BigDecimal(payment));
            //添加到订单表中
             orderMapper.insert(tbOrder);
        }
		//如果是在线支付则,保存一笔订单
		if (order.getPaymentType().equals("1")){
        	//创建payLog对象
			TbPayLog payLog = new TbPayLog();
			/*	`out_trade_no` varchar(30) NOT NULL COMMENT '支付订单号',   //分布式存储 idWorker
		  `create_time` datetime DEFAULT NULL COMMENT '创建日期',
		  `total_fee` bigint(20) DEFAULT NULL COMMENT '支付金额（分）',
		  `trade_state` varchar(1) DEFAULT NULL COMMENT '交易状态', //未支付状态
		  `user_id` varchar(50) DEFAULT NULL COMMENT '用户ID',
		  `order_list` varchar(200) DEFAULT NULL COMMENT '订单编号列表',  //一笔支付可能对应多笔订单  1,2,3
		  `pay_type` varchar(1) DEFAULT NULL COMMENT '支付类型',  //微信支付*/
			payLog.setOutTradeNo(idWorker.nextId()+"");
			payLog.setCreateTime(new Date());
			payLog.setTotalFee((long)(totalMoney*100));//转化为分
			payLog.setTradeState("1");
			payLog.setUserId(order.getUserId());
			//[1 , 2 , 3]我们通过切割的方式
			payLog.setOrderList(ids.toString().replace("[","").replace("]","").replace(" ",""));
			payLog.setPayType("1");
			//保存
			payLogMapper.insert(payLog);
			//将日志保存redis中
			redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);

		}
        //添加完,我们把redis中的购物车数据列表删除
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
