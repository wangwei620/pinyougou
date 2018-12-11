package com.pinyougou.user.service;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailService implements UserDetailsService {

    /**/
    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TbSeller tbSeller = sellerService.findOne(username);
        if (tbSeller!=null){
            //必须判断状态，只有审核通过的状态，才能正常登录
            if("1".equals(tbSeller.getStatus())){
                //构建用户权限集合数据
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
                //参数一:用户名   参数二;密码   权限集合
                return  new User(username,tbSeller.getPassword(),authorities);
            }else{
                return null;
            }

        }else {
            return null;
        }
    }
}
