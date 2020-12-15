package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author yuanpf
 * @create 2020-12-14 22:08
 */
@Component
public class AuthGlobalFilter implements GlobalFilter {
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${authUrls.url}")
    private String authUrls;
    private AntPathMatcher antPathMatcher = new AntPathMatcher();
    /**
     * 自定义的全局过滤器，过滤所有请求
     * @param exchange
     * @param chain
     * @return
     * a.	不允许访问内部调用接口 api/product/inner/getSkuInfo/{skuId}
     * b.	判断用户访问哪些控制器一定要登录 “trade.html”
     * c.	用户访问这样的 数据接口 /api/**//** 这样的接口，则必须登录！
     /api/auth/trade ： 表示下订单 {所以必须要获取用户Id，必须要登录}
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {


        //获取用户访问的是哪一个url
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        //判断path中是否符合验证规则
        if (antPathMatcher.match("/**/inner/**",path)){//如果满足验证的规则
            //设置响应
            ServerHttpResponse response = exchange.getResponse();
            return this.out(response, ResultCodeEnum.PERMISSION);//没有权限，
        }
        //获取用户id
        String userId = this.getUserId(request);
        //获取临时的用户id
        String userTempId = this.getUserTempId(request);
        if ("-1".equals(userId)){
            ServerHttpResponse response = exchange.getResponse();
            return out(response,ResultCodeEnum.PERMISSION);
        }

        if (!StringUtils.isEmpty(authUrls)){
            //访问符合规则的控制器时，提示必须登录以后才能继续操作  -------trade.html,myOrder.html,list.html  #需要限制的控制器，访问时必须登录以后才能继续访问
            String[] split = authUrls.split(",");
            if (split != null && split.length > 0){
                for (String url : split) {
                    //当前请求路径是authurls中的任何一个，并且用户未登录，则会进行拦截，
                    if (path.indexOf(url) != -1 && StringUtils.isEmpty(userId)){
                        //获取响应
                        ServerHttpResponse response = exchange.getResponse();
                        //设置请求码
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        //满足条件时设置请求头
                        response.getHeaders().set(HttpHeaders.LOCATION,"http://www.gmall.com/login.html?originUrl="+request.getURI());
                        //重定向到登录页面进行登录操作
                        return response.setComplete();
                    }
                }
            }

        }
            //[需要登录以后才能进行下面的操作]
        if (antPathMatcher.match("/api/**/auth/**",path)){//当请求的url满足该条件时，则必须登录
            if (StringUtils.isEmpty(userId)){//用户id为空，则表示未登录，必须进行登录
                //设置响应
                ServerHttpResponse response = exchange.getResponse();
                //
                return out(response,ResultCodeEnum.PERMISSION);//权限不够
            }
        }
        //将获取到的用户id传递到后台的微服务中使用，放在请求头里面
        if (!StringUtils.isEmpty(userId) || !StringUtils.isEmpty(userTempId)){
            if (!StringUtils.isEmpty(userId)){
                //用户已登录状态下添加购物车需要的用户id，放入request
                request.mutate().header("userId",userId).build();
                //将request变为exchange返回
//                return chain.filter(exchange.mutate().request(request).build());
            }else {
                //未登录情况下添加购物车需要的用户id,放入request
                request.mutate().header("userTempId",userTempId).build();
            }
            //将request变为exchange返回
            return chain.filter(exchange.mutate().request(request).build());
        }
        return chain.filter(exchange);//等价于返回Mono<Void>
    }

    private String getUserTempId(ServerHttpRequest request) {
        String userTempId = "";
        //先从请求头里面获取
        List<String> userTempIdlist = request.getHeaders().get("userTempId");
        if (!CollectionUtils.isEmpty(userTempIdlist)){//如果从请求头里面获取到了
            userTempId = userTempIdlist.get(0);//赋值
        }else {//请求头里面没有
            //再从cookie里面获取
            HttpCookie httpCookie = request.getCookies().getFirst("userTempId");
            if (httpCookie != null){
                userTempId = httpCookie.getValue();
            }
        }

        return userTempId;
    }


    //提示的方法
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum permission) {
        Result<Object> result = Result.build(null, permission);
        //转为json字符数组
        String strResult = JSON.toJSONString(result);
        //添加头信息
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        DataBuffer wrap = dataBufferFactory.wrap(strResult.getBytes());
        //返回数据
        return response.writeWith(Mono.just(wrap));
    }

    //获取用户id
    private String getUserId(ServerHttpRequest request) {
        //用户id存在于缓存中  userkey  = user:login:token   缓存中存放userid和ip组成的json字符串
        //因此先获取tokentoken
        String token = "";
        //从请求头里面获取token
        List<String> list = request.getHeaders().get("token");
        if (!CollectionUtils.isEmpty(list)){
            token = list.get(0);
        }
        //获取cookie
        List<HttpCookie> cookiesList = request.getCookies().get("token");
        //从cookie中获取token
        if (!CollectionUtils.isEmpty(cookiesList)){
            token =  cookiesList.get(0).getValue();
        }
        //组成key
        String userKey = "user:login:" + token;
        //从缓存中取数据
        String userJson = (String) redisTemplate.opsForValue().get(userKey);
        //将string转为json
        JSONObject jsonObject = JSON.parseObject(userJson);
        if (jsonObject != null){
            //获取用户ip
            String ip = (String) jsonObject.get("ip");
            if (ip.equals(IpUtil.getGatwayIpAddress(request))){//用户登录的ip和缓存中的ip一样
                //获取用户id
                String userId = (String) jsonObject.get("userId");
                return userId;
            }else {
                return "-1";
            }
        }
        return null;
    }
}
