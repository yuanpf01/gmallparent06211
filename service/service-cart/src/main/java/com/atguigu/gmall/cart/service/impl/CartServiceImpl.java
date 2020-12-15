package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartAsyncService;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author yuanpf
 * @create 2020-12-15 1:21
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartInfoMapper cartInfoMapper;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CartAsyncService cartAsyncService;

    //添加商品到购物车中
    @Override
    public void addToCart(Long skuId, String userId, Integer skuNum) {
        //定义商品在缓存中的key  user:userid:cart
        String cartKey = this.getCartKey(userId);
        //判断缓存中是否有数据
        if (!redisTemplate.hasKey(cartKey)){//如果没有，从数据库加载数据并放入缓存
            this.loadCartCache(userId);
        }
       //从缓存中获取数据
        CartInfo cartInfo = (CartInfo) redisTemplate.boundHashOps(cartKey).get(skuId.toString());

        if (cartInfo != null){
            //购物车中已经有该商品了
            cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);//更新购物车中商品的数量
            cartInfo.setCartPrice(productFeignClient.getSkuInfoPrice(skuId));//实时价格
            cartInfo.setUpdateTime(new Timestamp(new Date().getTime()));//更新时间
            cartAsyncService.updateCartInfo(cartInfo);//同步更新数据库中的数据
        }else {
            //当前购物车中没有该商品，第一次添加到购物车,并放入缓存
                CartInfo cartInfo1 = new CartInfo();
                SkuInfo skuinfo = productFeignClient.getSkuInfoAndImageBySkuId(skuId);
                cartInfo1.setSkuId(skuId);
                cartInfo1.setSkuNum(skuNum);
                cartInfo1.setSkuPrice(skuinfo.getPrice());
                cartInfo1.setCartPrice(skuinfo.getPrice());
                cartInfo1.setImgUrl(skuinfo.getSkuDefaultImg());
                cartInfo1.setCreateTime(new Timestamp(new Date().getTime()));
                cartInfo1.setUpdateTime(new Timestamp(new Date().getTime()));
                cartInfo1.setSkuName(skuinfo.getSkuName());
                cartInfo1.setUserId(userId);
                //添加到购物车
//                cartInfoMapper.insert(cartInfo1);
                cartAsyncService.saveCartInfo(cartInfo1);
                //优化一，使用异步编排
//              cartAsyncService.saveCartInfo(cartInfo1);
                //放入缓存
                //        redisTemplate.opsForHash().put(cartKey,skuId.toString(),cartInfo1);
                //redisTemplate.boundHashOps(cartKey).put(cartInfo.getSkuId().toString(),cartInfo1);
                cartInfo=cartInfo1;
                //设置数据在缓存中的过期时间，，，redis做优化，mysql做持久化
                //this.setCartKeyExpire(cartKey);
        }
        redisTemplate.boundHashOps(cartKey).put(skuId.toString(),cartInfo);
        //设置过期时间
        this.setCartKeyExpire(cartKey);
     }

    /**
     * 查询购物车中的商品信息
     * @param userId
     * @param  userTempId
     * @return
     */
    @Override
    public List<CartInfo> getCartInfoList(String userId, String userTempId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        //根据userId查询缓存，如果缓存中没有数据，从数据库中查询并写入缓存中
        //登录时的购物车中的商品信息,方法 的重载
            /**
             * 合并购物车的条件：
             * 登录情况下，并且登录之前的购物车中有数据，此时，必须合并购物车，
             * 具体实现：
             * 1、如果未登录的购物车中的商品在登录后的购物车中存在，对数据更新、
             * 2、如果未登录的购物车中的商品在登录后的购物车中不存在，添加数据
             * 3、合并之后默认选中的规则：以未登录的购物车中的选中状态为基准
             *                        以登录后的购物车中的选中状态为基准
             *                        只要有任意一个购物车中是选中的，合并后即默认选中
             * 4、合并购物车之后清空登陆之前购物车中的数据
             */
        if (!StringUtils.isEmpty(userId)){//userid != null
            if (StringUtils.isEmpty(userTempId)){//登录之前没有购物车
                cartInfoList = getCartInfoList(userId);
            }
            if (!StringUtils.isEmpty(userTempId)){//登录之前有购物车
                //获取登录前购物车中的数据
                List<CartInfo> userLoginBeforeList = this.getCartInfoList(userTempId);
                if (!CollectionUtils.isEmpty(userLoginBeforeList)){//userBeforeList != null && userId != null如果登录之前的购物车中有数据，合并购物车
                    cartInfoList = this.mergeToCartList(userLoginBeforeList,userId);
                    //合并购物车之后清空登陆之前购物车中的数据
                    this.cleanLoginBeforeCart(userTempId);
                }else {//userid != null && usertempid != null &&  userBeforeList = null 不需要合并
                    cartInfoList = this.getCartInfoList(userId);
                }
            }else {//userid != null  && usertempId == null 不需要合并购物车
                cartInfoList =  this.getCartInfoList(userId);
            }
        }else {//userid = null  //未登录时购物车中的商品的信息 不需要合并购物车
            cartInfoList = getCartInfoList(userTempId);    // 不需要合并购物车
        }
        return cartInfoList;
    }

    @Override//更新选中的状态
    public void checkCart(String userId, Integer isChecked, Long skuId) {
       cartAsyncService.checkCart(userId,isChecked,skuId);//异步
        //更新缓存
        //获取在缓存中的key
        String cartKey = this.getCartKey(userId);
        //获取缓存中的数据
        CartInfo cartInfo  = (CartInfo) redisTemplate.opsForHash().get(cartKey, skuId.toString());
        cartInfo.setIsChecked(isChecked);
        //放入缓存
        redisTemplate.opsForHash().put(cartKey,skuId.toString(),cartInfo);
        //更新过期时间
        this.setCartKeyExpire(cartKey);

    }

    //清空登录之前的购物车的方法  清空临时购物车、清空缓存
    private void cleanLoginBeforeCart(String userTempId) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userTempId);
        cartAsyncService.delete(cartInfo);
        //清空缓存
        //获取key
        String cartKey = this.getCartKey(userTempId);
        redisTemplate.delete(cartKey);
    }

    //合并购物车的方法
    private List<CartInfo> mergeToCartList(List<CartInfo> userLoginBeforeList , String userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        //获取登录之后的购物车中的数据
        List<CartInfo> userLoginAfterList  = this.getCartInfoList(userId);
        if (!CollectionUtils.isEmpty(userLoginAfterList)){//登录之后的购物车中有数据，合并
            //将登录之后购物车中的数据类型站换位map类型，通过key得到value进行合并
            //Function    R apply(T t); 有参数有返回值 参数cartinfo
            //function---->参数CartInfo::返回值getSkuId【key的类型】, Function-->参数CartInfofu -> 返回值CartInfo) 【value
            Map<Long, CartInfo> loginAfterValuesMap = userLoginAfterList.stream().collect(Collectors.toMap(CartInfo::getSkuId,CartInfo->CartInfo));
            //遍历登录之前购物车中的数据，用来和登录之后购物车中的数据比较
            for (CartInfo cartInfo : userLoginBeforeList) {
                //获取登录之前购物车中的商品的skuId用来和登录之后购物车中的sku比较
                Long skuId = cartInfo.getSkuId();
                if (loginAfterValuesMap.containsKey(skuId)){//loginAfterValuesMap的key就是skuid
                    //该商品已经在登录之后的购物车中存在了，只需要改变登录之后购物车中的数量和更新修改的时间
                    CartInfo loginAfterCartInfo = loginAfterValuesMap.get(skuId);
                    loginAfterCartInfo.setSkuNum(loginAfterCartInfo.getSkuNum() + cartInfo.getSkuNum());
                    loginAfterCartInfo.setUpdateTime(new Timestamp(new Date().getTime()));

                    //合并购物车后商品的选中状态，以未登录的为基准，ischecked
                    if (cartInfo.getIsChecked() == 1){
                        loginAfterCartInfo.setIsChecked(1);
                    }
                    //更新数据库
                    cartInfoMapper.updateById(loginAfterCartInfo);//同步
                   // cartAsyncService.updateCartInfo(loginAfterCartInfo);//异步更新，数据不一致
                }else {//二者skuid不一致。添加到登录后的购物车中
                    //赋值
                    cartInfo.setUserId(userId);
                    cartInfo.setCreateTime(new Timestamp(new Date().getTime()));
                    cartInfo.setUpdateTime(new Timestamp(new Date().getTime()));
                    //将登录前购物车中独有的商品添加到登录后的购物车里面
                    cartInfoMapper.insert(cartInfo);
                }
            }
            //合并完成，查询数据库更新缓存
            this.loadCartCache(userId);
        }else {//登录之后的购物车中没有数据
            cartInfoList = userLoginBeforeList;
            return cartInfoList;
        }
        return cartInfoList;
    }

    //根据userId查询缓存，如果缓存中没有数据，从数据库中查询并写入缓存中
    private List<CartInfo> getCartInfoList(String userId) {
        List<CartInfo> cartInfos = new ArrayList<>();
        if (StringUtils.isEmpty(userId)){//临时id和用户登录的id都为空，直接返回
            return cartInfos;
        }
        //查询缓存
        //获取缓存中的key
        String cartKey = this.getCartKey(userId);
        cartInfos = redisTemplate.boundHashOps(cartKey).values();
        if (!CollectionUtils.isEmpty(cartInfos)){//缓存中有数据
            //对购物车中的商品按照更新时间进行排序
            cartInfos.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    // o2.getUpdateTime().equals(o1.getUpdateTime()) 内部实现，compareable外部实现
                    //Calendar.SECOND,将比较的年月日，时分秒，转为秒单位去比较
                    return DateUtil.truncatedCompareTo(o2.getUpdateTime(),o1.getUpdateTime(),Calendar.SECOND);
                }
            });
            return cartInfos;
        }else {//缓存中没有,查询数据库，写入缓存
            cartInfos = this.loadCartCache(userId);
            return cartInfos;
        }
    }

    //设置数据在缓存中的过期时间，，，redis做优化，mysql做持久化
    private void setCartKeyExpire(String cartKey) {
        redisTemplate.expire(cartKey, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }

    //更新缓存中的数据
    private List<CartInfo> loadCartCache(String userId) {
        //获取缓存中的key
        String cartKey = this.getCartKey(userId);
        //从数据库中获取数据
        QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();
        cartInfoQueryWrapper.eq("user_id",userId);
        List<CartInfo> cartInfoList = cartInfoMapper.selectList(cartInfoQueryWrapper);
        if (CollectionUtils.isEmpty(cartInfoList)){//如果数据库中的数据为空
            return cartInfoList;
        }else {
            //遍历将数据放入缓存中
            HashMap<String, Object> map = new HashMap<>();
            for (CartInfo cartInfo : cartInfoList) {
                //更新价格
                cartInfo.setCartPrice(productFeignClient.getSkuInfoPrice(cartInfo.getSkuId()));
                //往缓存中放数据
                //redisTemplate.boundHashOps(cartKey).put(cartInfo.getSkuId().toString(),cartInfo);效果和下面的方法一样
                map.put(cartInfo.getSkuId().toString(),cartInfo);
            }
            redisTemplate.boundHashOps(cartKey).putAll(map);
            //设置一个过期时间
            this.setCartKeyExpire(cartKey);
            return cartInfoList;
        }
    }

    //获取购物车商品在缓存中的key  user:userid:cart
    private String getCartKey(String userId) {
            return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
        }
    }
