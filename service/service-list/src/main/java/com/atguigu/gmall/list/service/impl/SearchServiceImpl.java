package com.atguigu.gmall.list.service.impl;

import com.atguigu.gmall.list.service.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author yuanpf
 * @create 2020-12-09 23:18
 */
@Service
public class SearchServiceImpl  implements SearchService {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private GoodsRepository goodsRepository;

    //
    @Autowired
    private RedisTemplate redisTemplate;

    //上架
    @Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();
        //调用product ,根据skuid查询得到skuinfo + baseattr + baseattrvalue,封装到goods里面

        //异步编排
        CompletableFuture<SkuInfo> skuInfoAndImageCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //skuinfo数据和图片
            SkuInfo skuInfo = productFeignClient.getSkuInfoAndImageBySkuId(skuId);
            goods.setId(skuId);
            goods.setTitle(skuInfo.getSkuName());
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());
            goods.setPrice(skuInfo.getPrice().doubleValue());
            goods.setCreateTime(new Date());
            return skuInfo;
        });

        CompletableFuture<Void> baseCategoryViewcompletableFuture = skuInfoAndImageCompletableFuture.thenAcceptAsync((skuInfo) -> {
            //根据三级分类的id获取一级、二级、三级分类的id和名称
            BaseCategoryView baseCategoryViewList = productFeignClient.getBaseCategoryViewList(skuInfo.getCategory3Id());
            goods.setCategory1Id(baseCategoryViewList.getCategory1Id());
            goods.setCategory2Id(baseCategoryViewList.getCategory2Id());
            goods.setCategory3Id(baseCategoryViewList.getCategory3Id());
            goods.setCategory1Name(baseCategoryViewList.getCategory1Name());
            goods.setCategory2Name(baseCategoryViewList.getCategory2Name());
            goods.setCategory3Name(baseCategoryViewList.getCategory3Name());
        });


        CompletableFuture<Void> baseAttrInfoCompletableFuture = CompletableFuture.runAsync(() -> {
            //平台属性名 +平台属性值searAttrList
            List<BaseAttrInfo> baseAttrInfoList = productFeignClient.getBaseAttrInfo(skuId);
            /*List<SearchAttr> collect = baseAttrInfoList.stream().map((baseAttrInfo) -> {
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(baseAttrInfo.getId());
                searchAttr.setAttrName(baseAttrInfo.getAttrName());
                searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
//                searchAttrList.add(searchAttr);
                return searchAttr;
            }).collect(Collectors.toList());*/
            List<SearchAttr> searchAttrList = new ArrayList<>();
            baseAttrInfoList.stream().forEach((baseAttrInfo)->{
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(baseAttrInfo.getId());
                searchAttr.setAttrName(baseAttrInfo.getAttrName());
                searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
//              searchAttrList.add(searchAttr);
            });
            // goods.setAttrs(collect);
            goods.setAttrs(searchAttrList);
        });
        CompletableFuture<Void> trademarkDataCompletableFuture = skuInfoAndImageCompletableFuture.thenAcceptAsync((skuInfo) -> {
            //品牌数据
            BaseTrademark trademarkData = productFeignClient.getTrademarkData(skuInfo.getTmId());
            goods.setTmId(skuInfo.getTmId());
            goods.setTmName(trademarkData.getTmName());
            goods.setTmLogoUrl(trademarkData.getLogoUrl());
        });
        CompletableFuture.allOf(
                skuInfoAndImageCompletableFuture,
                baseCategoryViewcompletableFuture,
                baseAttrInfoCompletableFuture,
                trademarkDataCompletableFuture
        ).join();
        //保存
        this.goodsRepository.save(goods);
    }

    @Override//下架
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    //更新热度排名
    public void incrHotScore(Long skuId){
        //确定存储的数据类型--->zset
        String  hotScoreKey =  "hotScore";
        Double hotScore = redisTemplate.opsForZSet().incrementScore(hotScoreKey, "skuId:" + skuId,1);
        if (hotScore % 10 == 0){//判断
            Optional<Goods> optional = this.goodsRepository.findById(skuId);
            Goods goods = optional.get();
            goods.setHotScore(hotScore.longValue());
            this.goodsRepository.save(goods);
        }

    }
}
