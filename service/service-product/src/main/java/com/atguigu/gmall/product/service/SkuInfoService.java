package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Map;

/**
 * @author yuanpf
 * @create 2020-12-02 16:20
 */
public interface SkuInfoService {

    /**
     * 保存skuinfo
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 分页查询所有skuinfo
     * @param skuInfoPage
     * @return
     */
    IPage<SkuInfo> getSkuInfoPageList(Page<SkuInfo> skuInfoPage);

    /**
     * 上架
     * @param skuId
     */
    void skuOnSaleService(Long skuId);

    /**
     * 下架
     * @param skuId
     */

    void skuCancelSaleService(Long skuId);
    /**
     * 点击商品进入商品详情页，
     * 根据spuid查询所有的skuid
     * 点击商品的销售属性id获得商品的skuid查询得到商品的销售属性值并切换
     */
    Map selectSkuSaleValueIdsMapBySpuId(Long spuId);
}
