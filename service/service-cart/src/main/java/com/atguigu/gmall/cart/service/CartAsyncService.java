package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

/**
 * @author yuanpf
 * @create 2020-12-15 13:19
 */
public interface CartAsyncService {
    /**
     * 更新购物车
     */
    void updateCartInfo(CartInfo cartInfo);
    /**
     * 添加商品到购物车
     */
    void saveCartInfo(CartInfo cartInfo);

    void delete(CartInfo cartInfo);//清空临时购物车

    //点击之后选中状态的改变
    void checkCart(String userId, Integer isChecked, Long skuId);


}
