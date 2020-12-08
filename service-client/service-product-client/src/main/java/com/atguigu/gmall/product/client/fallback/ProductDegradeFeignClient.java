package com.atguigu.gmall.product.client.fallback;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author yuanpf
 * @create 2020-12-05 22:50
 */
@Component
public class ProductDegradeFeignClient implements ProductFeignClient {
    @Override
    public SkuInfo getSkuInfoAndImageBySkuId(Long skuId) {

        return null;
    }

    @Override
    public BaseCategoryView getBaseCategoryViewList(Long category3Id) {
        return null;
    }

    @Override
    public BigDecimal getSkuInfoPrice(Long skuId) {
        return null;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrAndValueById(Long skuId, Long spuId) {
        return null;
    }

    @Override
    public Map getSkuSaleValueIdsMapBySpuId(Long spuId) {
        System.out.println("兜底方法执行了");
        return null;
    }

    @Override
    public Result getBaseCategoryList() {
        return null;
    }
}
