package com.atguigu.gmall.product.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.ManageService;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.atguigu.gmall.product.service.SpuInfoService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author yuanpf
 * @create 2020-12-04 16:18
 */
@RestController
@RequestMapping("api/product")
@Api(tags = "为item提供数据的接口")
public class ProdectApiController {
    @Autowired
    ManageService manageService;
    @Autowired
    SpuInfoService spuInfoService;
    @Autowired
    SkuInfoService skuInfoService;
    /**
     * 根据skuid提供商品基本属性和图片的接口
     */
    @GetMapping("inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfoAndImageBySkuId(@PathVariable Long skuId){

        return manageService.getSkuInfoBySkuId(skuId);
    }
    /**
     * 需求分析：
     * 三级分类id。在skuinfo中提供
     * 根据三级分类id获取三级分类名称、二级分类id
     * 根据二级分类id获取二级分类名称、一级分类id
     * 根据一级分类id获取一级分类名称
     *具体实现：
     * 通过三级分类id查询一级分类、二级分类、三级分类的视图获取数据
     */
    @GetMapping("inner/getCategoryView/{category3Id}")
    public BaseCategoryView getBaseCategoryViewList(@PathVariable Long category3Id){
        return manageService.getBaseCategoryName(category3Id);
    }
    /**
     * 根据skuid获取最新的价格信息
     */
    @GetMapping("inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuInfoPrice(@PathVariable Long skuId){
        return manageService.getSkuPriceBySkuId(skuId);
    }
    /**
     * 根据spuid skuid 获取销售属性和销售属性值并锁定
     */
    @GetMapping("inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrAndValueById(
                                                        @PathVariable Long skuId,
                                                        @PathVariable Long spuId
                                                         ){
        return spuInfoService.getSpuSaleAttrAndValues(skuId,spuId);
    }
    //根据spuid查询得到当前商品所有的所有skuid作为value,销售属性值id作为key存放在map集合中@GetMapping("inner/getSkuValueIdsMap/{spuId}")
    @GetMapping("inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuSaleValueIdsMapBySpuId(@PathVariable Long spuId){
        return skuInfoService.selectSkuSaleValueIdsMapBySpuId(spuId);
    }
    /**
     * 获取首页所有的分类数据
     */
    @GetMapping("getBaseCategoryList")
    public Result getBaseCategoryList(){
        List<JSONObject> list = manageService.getBaseCategoryList();
        return Result.ok(list);
    }

}
