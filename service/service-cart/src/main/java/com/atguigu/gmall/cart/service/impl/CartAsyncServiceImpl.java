package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartAsyncService;
import com.atguigu.gmall.model.cart.CartInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author yuanpf
 * @create 2020-12-15 13:19
 */
@Service
public class CartAsyncServiceImpl implements CartAsyncService {
    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Override
    @Async  //使用异步进行处理，主启动类必须添加@EnableAsync
    public void updateCartInfo(CartInfo cartInfo) {
        QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();
        cartInfoQueryWrapper.eq("sku_id",cartInfo.getSkuId());
        cartInfoQueryWrapper.eq("user_id",cartInfo.getUserId());
        cartInfoMapper.update(cartInfo,cartInfoQueryWrapper);
    }

    @Override
    @Async//使用异步进行处理，主启动类必须添加@EnableAsync
    public void saveCartInfo(CartInfo cartInfo) {
        cartInfoMapper.insert(cartInfo);
    }

    //清空临时购物车
    @Override
    @Async
    public void delete(CartInfo cartInfo) {
        QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();
        cartInfoQueryWrapper.eq("user_id",cartInfo.getUserId());
//        cartInfoQueryWrapper.eq("sku_id",cartInfo.getSkuId());
        cartInfoMapper.delete(cartInfoQueryWrapper);
    }

    @Override//点击之后选中状态的改变
    @Async
    public void checkCart(String userId, Integer isChecked, Long skuId) {
        CartInfo cartInfo1 = new CartInfo();
        cartInfo1.setIsChecked(isChecked);
        QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();
        cartInfoQueryWrapper.eq("user_id",userId);
        cartInfoQueryWrapper.eq("sku_id",skuId);
        cartInfoMapper.update(cartInfo1,cartInfoQueryWrapper);
    }
}
