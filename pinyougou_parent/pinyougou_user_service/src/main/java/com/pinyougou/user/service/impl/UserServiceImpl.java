package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.entity.PageResult;
import com.pinyougou.mapper.TbUserMapper;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.pojo.TbUserExample;
import com.pinyougou.pojo.TbUserExample.Criteria;
import com.pinyougou.user.service.UserService;
import com.pinyougou.util.HttpClient;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private TbUserMapper userMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbUser> findAll() {
		return userMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbUser> page=   (Page<TbUser>) userMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbUser user) {

//		`password` varchar(32) NOT NULL COMMENT '密码，加密存储',
//			 `created` datetime NOT NULL COMMENT '创建时间',
//			 `updated` datetime NOT NULL,
//			 `source_type` varchar(1) DEFAULT NULL COMMENT '会员来源：1:PC
//			 `status` varchar(1) DEFAULT NULL COMMENT '使用状态（Y正常 N非正常）',  //正常状态
//			 `is_mobile_check` varchar(1) DEFAULT '0' COMMENT '手机是否验证 （0否  1是）',   //1是
		//我们后台封装数据
		String password = DigestUtils.md5Hex(user.getPassword());
		user.setPassword(password);
		user.setCreated(new Date());
		user.setUpdated(new Date());
		user.setSourceType("1");
		user.setStatus("Y");
		user.setIsMobileCheck("1");
		userMapper.insert(user);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbUser user){
		userMapper.updateByPrimaryKey(user);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbUser findOne(Long id){
		return userMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			userMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbUser user, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbUserExample example=new TbUserExample();
		Criteria criteria = example.createCriteria();
		
		if(user!=null){			
						if(user.getUsername()!=null && user.getUsername().length()>0){
				criteria.andUsernameLike("%"+user.getUsername()+"%");
			}
			if(user.getPassword()!=null && user.getPassword().length()>0){
				criteria.andPasswordLike("%"+user.getPassword()+"%");
			}
			if(user.getPhone()!=null && user.getPhone().length()>0){
				criteria.andPhoneLike("%"+user.getPhone()+"%");
			}
			if(user.getEmail()!=null && user.getEmail().length()>0){
				criteria.andEmailLike("%"+user.getEmail()+"%");
			}
			if(user.getSourceType()!=null && user.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+user.getSourceType()+"%");
			}
			if(user.getNickName()!=null && user.getNickName().length()>0){
				criteria.andNickNameLike("%"+user.getNickName()+"%");
			}
			if(user.getName()!=null && user.getName().length()>0){
				criteria.andNameLike("%"+user.getName()+"%");
			}
			if(user.getStatus()!=null && user.getStatus().length()>0){
				criteria.andStatusLike("%"+user.getStatus()+"%");
			}
			if(user.getHeadPic()!=null && user.getHeadPic().length()>0){
				criteria.andHeadPicLike("%"+user.getHeadPic()+"%");
			}
			if(user.getQq()!=null && user.getQq().length()>0){
				criteria.andQqLike("%"+user.getQq()+"%");
			}
			if(user.getIsMobileCheck()!=null && user.getIsMobileCheck().length()>0){
				criteria.andIsMobileCheckLike("%"+user.getIsMobileCheck()+"%");
			}
			if(user.getIsEmailCheck()!=null && user.getIsEmailCheck().length()>0){
				criteria.andIsEmailCheckLike("%"+user.getIsEmailCheck()+"%");
			}
			if(user.getSex()!=null && user.getSex().length()>0){
				criteria.andSexLike("%"+user.getSex()+"%");
			}
	
		}
		
		Page<TbUser> page= (Page<TbUser>)userMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 发送验证码
	 * @param phone
	 */
	@Override
	public void sendSmsCode(String phone) throws IOException, ParseException {
		//1.随机成功六位数字验证码
		int num =(int)(Math.random()*9+1);
		String smsCode = RandomStringUtils.randomNumeric(5);
		smsCode=num+smsCode;
		//2.将生成的验证码存到redis中
		redisTemplate.boundValueOps(phone).set(smsCode,5L, TimeUnit.MINUTES);
		//3.调用短信发送的接口进行发送
		HttpClient httpClient = new HttpClient("http://localhost:7788/sms/sendSms.do");
		httpClient.addParameter("phoneNumbers",phone);
		httpClient.addParameter("signName","品优购");
		httpClient.addParameter("templateCode","SMS_123738164");
		httpClient.addParameter("param","{\"code\":"+smsCode+"}");
		//发送
		httpClient.post();
		//获取返回的消息
		String content = httpClient.getContent();
		System.out.println(content);
		if (content==null){
			throw new RuntimeException("调用接口失败");
		}
	}

    @Override
    public boolean checkSmsCode(String phone, String smsCode) {
        //从redis中获取验证码
        String sysCode = (String) redisTemplate.boundValueOps(phone).get();
        //判断
        if (sysCode==null){//取不到过期了
            return false;
        }
        if (!sysCode.equals(smsCode)){
            return false;
        }

        return true;
    }

}
