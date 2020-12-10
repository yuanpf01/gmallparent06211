package com.atguigu.gmall.product.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.fallback.ProductDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author yuanpf
 * @create 2020-12-05 22:44
 */
@FeignClient(name = "service-product",fallback = ProductDegradeFeignClient.class)
public interface ProductFeignClient {

    /**
     * 根据skuid提供商品基本属性和图片的接口
     */
    @GetMapping("api/product/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfoAndImageBySkuId(@PathVariable Long skuId);
    /**
     * 需求分析：
     * 三级分类id。在skuinfo中提供
     * 根据三级分类id获取三级分类名称、二级分类id
     * 根据二级分类id获取二级分类名称、一级分类id
     * 根据一级分类id获取一级分类名称
     *具体实现：
     * 通过三级分类id查询一级分类、二级分类、三级分类的视图获取数据
     */
    @GetMapping("api/product/inner/getCategoryView/{category3Id}")
    public BaseCategoryView getBaseCategoryViewList(@PathVariable Long category3Id);
    /**
     * 根据skuid获取最新的价格信息
     */
    @GetMapping("api/product/inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuInfoPrice(@PathVariable Long skuId);
    /**
     * 根据spuid skuid 获取销售属性和销售属性值并锁定
     */
    @GetMapping("api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrAndValueById(
            @PathVariable Long skuId,
            @PathVariable Long spuId
    );
    //根据spuid查询得到当前商品所有的所有skuid作为value,销售属性值id作为key存放在map集合中
    @GetMapping("api/product/inner/getSkuValueIdsMap/{spuId}")
    Map getSkuSaleValueIdsMapBySpuId(@PathVariable Long spuId);

    /**
     * 获取首页所有的分类数据
     */
    @GetMapping("api/product/getBaseCategoryList")
    public Result getBaseCategoryList();


    /**
     * 根据skuid获取平台属性，平台属性值/getAttrList/{skuId}
     */
    @GetMapping("api/product/getAttrList/{skuId}")
    public List<BaseAttrInfo> getBaseAttrInfo(@PathVariable Long skuId);

    //根据品牌id获取品牌信息
    @GetMapping("api/product/inner/getTrademark/{tmId}")
    public BaseTrademark getTrademarkData(@PathVariable Long tmId);
}
