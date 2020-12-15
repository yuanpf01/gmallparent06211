package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author yuanpf
 * @create 2020-12-15 1:19
 */
@Api(tags = "添加购物车的api接口")
@RestController
@RequestMapping("api/cart")
public class CartApiController {
    @Autowired
    CartService cartService;

    /**
     * 添加到购物车
     * @param skuId
     * @param skuNum
     * @param request
     * @return
     * http://cart.gmall.com/addCart.html【web中的控制器】?skuId=' + this.skuId + '&skuNum=' + this.skuNum
     */
    @PostMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable("skuId") Long skuId,
                            @PathVariable("skuNum") Integer skuNum,
                            HttpServletRequest request) {
        /**
         * 1、添加到购物车之前，购物车已经存在该商品
         * 数量增加，创建时间修改时间更新
         *
         * 2、添加之前购物车里面没有该商品
         * 直接加入购物车
         * 3、添加到redis缓存中
         */
        //获取用户id
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)){
            //如果用户id为空，获取一个临时id，用于在未登录时添加购物车进行区分账户
            userId  = AuthContextHolder.getUserTempId(request);//String userTempId
        }
        //添加到购物车
        cartService.addToCart(skuId,userId,skuNum);

        return Result.ok();
       }

    /**
     *
     *根据用户id查询购物车列表信息
     * return request({
     *       url: this.api_name + '/cartList',
     *       method: 'get'
     *     })
     */
    @GetMapping("cartList")
    public Result getCartList(HttpServletRequest request){
        //得到用户id
        String userId = AuthContextHolder.getUserId(request);
        String userTempId = AuthContextHolder.getUserTempId(request);
        List<CartInfo> cartInfoList = cartService.getCartInfoList(userId, userTempId);
        return Result.ok(cartInfoList);
    }
    /**
     * 跟新选中状态
     * checkCart(skuId, isChecked) {
     *     return request({
     *       url: this.api_name + '/checkCart/' + skuId + '/' + isChecked,
     *       method: 'get'
     *     })
     */
    @GetMapping("/checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId,
                            @PathVariable Integer isChecked,
                            HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        String userTempId = AuthContextHolder.getUserTempId(request);
        if (StringUtils.isEmpty(userId)){
            userId = userTempId;//如果userid为空，赋值为usertempid
        }
        //调用方法
        cartService.checkCart(userId,isChecked,skuId);
        return Result.ok();
    }


    }
