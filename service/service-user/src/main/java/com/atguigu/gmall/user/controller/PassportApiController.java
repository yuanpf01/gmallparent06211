package com.atguigu.gmall.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户认证接口
 * </p>
 *
 */
@RestController
@RequestMapping("/api/user/passport")
public class PassportApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 单点登录
     *
     * @param userInfo
     * @param request
     * @return
     * * login(userInfo) {
     *      *     return request({
     *      *       url: this.api_name + '/login',
     *      *       method: 'post',
     *      *       data: userInfo
     *      *     })
     */
    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo, HttpServletRequest request){
        //  调用登录方法
        UserInfo info = userService.login(userInfo);

        //  如果登录成功  前台提交用户名和密码到url: this.api_name + '/login',post， userInfo
        if (info!=null){
            //  需要token，response.data.data 第二个data 是一个对象 或者叫map .
            //  制作一个token
            String token = UUID.randomUUID().toString();
            //  声明一个map，将用户昵称和token放在map里面，前端将token和用户昵称放在cookie中
            HashMap<String, Object> map = new HashMap<>();
            map.put("token",token);
            map.put("nickName",info.getNickName());

            //  用什么数据类型合适String ，如何起Key {token}, 到底要存储什么数据到缓存? userId , 存储一个IP地址防止token 盗用
            //  set(token:userId , {"userId":"1","Ip":"192.168.200.1"})

            //  将userId，ip 地址存储一个Json 字符串 存放在缓存中
            JSONObject userJson = new JSONObject();
            userJson.put("userId",info.getId().toString());
            userJson.put("ip", IpUtil.getIpAddress(request));
            //  定义key=user:login:token
            String userkey = RedisConst.USER_LOGIN_KEY_PREFIX + token;
            //将用户信息存放在缓存中 userId 、ip 组成的json字符串
            redisTemplate.opsForValue().set(userkey,userJson.toJSONString(),RedisConst.USERKEY_TIMEOUT,TimeUnit.SECONDS);
            //  返回数据
            return Result.ok(map);
        }else {
            return Result.fail().message("登录失败！");
        }
    }
    /**
     * 退出登录
     * http://api.gmall.com/api/user/passport/logout
     *
     */
    @GetMapping("logout")
    public Result logout(HttpServletRequest request){
        /**
         * 1、删除缓存中用户的信息
         * 2、删除cookie中用户的信息
         * logout() {
         *     return request({
         *       url: this.api_name + '/logout',
         *       method: 'get'
         *     })
         */
        //得到缓存中的key---->需要先得到请求头中的token---->然后的到key
        String token = request.getHeader("token");
        String userKey = RedisConst.USER_LOGIN_KEY_PREFIX + token;
        if (userKey != null){
            Boolean flag = redisTemplate.delete(userKey);
            if (flag) {
                return Result.ok();
            }
        }
            return Result.fail();
    }

}