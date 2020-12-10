package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.client.ListFeignClient;
import com.atguigu.gmall.item.config.MyThreadPoolExcute;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
/**
 * @author yuanpf
 * @create 2020-12-05 22:58
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired//远程调用product微服务，获取数据进行数据汇总
    private ProductFeignClient productFeignClient;

    @Autowired
    private MyThreadPoolExcute myThreadPoolExcute;

    @Autowired
    private ListFeignClient listFeignClient;

    @Override
    public Map<String,Object> getBySkuId(Long skuId){
        HashMap<String, Object> map = new HashMap<>();
        //异步编排：通过多线程异步进行不同的任务，实现线程的串行化和并行化，提升效率
        //supplyAsync:支持返回值
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //根据skuid获取skuinfo和imagelist
            SkuInfo skuInfoAndImage = productFeignClient.getSkuInfoAndImageBySkuId(skuId);
            // 保存skuInfo
            map.put("skuInfo",skuInfoAndImage);
            return skuInfoAndImage;
        });

        //runAsync:不支持返回值，不需要其他线程的返回值
        CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
            //根据skuid获取最新价格
            BigDecimal skuInfoPrice = productFeignClient.getSkuInfoPrice(skuId);
            // 获取价格
            map.put("price",skuInfoPrice);
        });

        //thenAcceptAsync获取上一个进程的返回值并使用
        //Consumer   void accept(T t);
        CompletableFuture<Void> baseCategoryViewListCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfoAndImage -> {
            //根据三级分类id获取一级分类、二级分类、三级分类名称的集合
            BaseCategoryView baseCategoryViewList = productFeignClient.getBaseCategoryViewList(skuInfoAndImage.getCategory3Id());
            //保存商品分类数据
            map.put("categoryView",baseCategoryViewList);
        }));

        CompletableFuture<Void> spuSaleAttrAndValueCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfoAndImage -> {
            //根据skuid,spuid获取销售属性和销售属性值并锁定
            List<SpuSaleAttr> spuSaleAttrAndValue = productFeignClient.getSpuSaleAttrAndValueById(skuId, skuInfoAndImage.getSpuId());
            // 保存数据
            map.put("spuSaleAttrList",spuSaleAttrAndValue);
        }));

        CompletableFuture<Void> skuSaleValueIdsMapCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfoAndImage -> {
        //获取切换的数据【点击销售属性，根据销售属性id获得skuid,得到销售属性和销售属性值的信息】
            //并转换为字符串
            Map skuSaleValueIdsMap = productFeignClient.getSkuSaleValueIdsMapBySpuId(skuInfoAndImage.getSpuId());
            String mapToJson = JSON.toJSONString(skuSaleValueIdsMap);
            // 保存valuesSkuJson
            map.put("valuesSkuJson",mapToJson);
        }));
        CompletableFuture<Void> incrHotScoreCompletableFuture = CompletableFuture.runAsync(() -> {
            listFeignClient.incrHotScore(skuId);
        });

//        // 保存 json字符串        异常点：json转换两次，无法实现跳转
//        String valuesSkuJson = JSON.toJSONString(mapToJson);
        //多任务组合的方法allof()
        CompletableFuture.allOf(skuInfoCompletableFuture,
                                priceCompletableFuture,
                                baseCategoryViewListCompletableFuture,
                                spuSaleAttrAndValueCompletableFuture,
                                skuSaleValueIdsMapCompletableFuture,
                                incrHotScoreCompletableFuture).join();
        return map;
    }
}
