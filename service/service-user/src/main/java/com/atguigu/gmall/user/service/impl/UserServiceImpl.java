package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * @author yuanpf
 * @create 2020-12-13 23:42
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserInfoMapper userInfoMapper;
    @Override
    public UserInfo login(UserInfo userInfo) {
        //  select * from user_info where login_name = ? and passwd = ?
        //  使用md5对密码进行加密
        String newPwd = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("login_name",userInfo.getLoginName());//用户输入的登录名称
        userInfoQueryWrapper.eq("passwd",newPwd);//密码
        //调用mapper曾方法进行查询
        UserInfo info = userInfoMapper.selectOne(userInfoQueryWrapper);
        //  判断当前对象是否为空
        if (info!=null){//登录成功
            return info;
        }
        return null;
    }
}
