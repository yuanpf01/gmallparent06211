package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author yuanpf
 * @create 2020-12-01 23:53
 */
public interface SpuInfoService extends IService<SpuInfo> {

    /**
     * 添加spuinfo
     * @param spuInfo
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 根据spuid获取spu消费属性
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);
    /**
     * 根据spuid获取图片列表
     * http://api.gmall.com/admin/product/spuImageList/{spuId}
     *
     */

    List<SpuImage> getSpuImageList(Long spuId);
    /**
     * 根据skuid和spuid获取销售属性和销售属性值并且锁定
     */
    List<SpuSaleAttr> getSpuSaleAttrAndValues(Long skuId,Long spuId);

}
