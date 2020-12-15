package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

/**
 * @author yuanpf
 * @create 2020-12-15 1:21
 */
public interface CartService {

   void  addToCart(Long skuId, String userId, Integer skuNum);//添加到购物车

   //查询购物车中的商品
   List<CartInfo> getCartInfoList(String userId,String userTempId);
   //点击之后选中状态的改变
   void checkCart(String userId, Integer isChecked, Long skuId);

}
