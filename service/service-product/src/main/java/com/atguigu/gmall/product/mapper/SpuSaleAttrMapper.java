package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author yuanpf
 * @create 2020-12-02 13:18
 */
@Mapper
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {
    /**
     * 根据skuid和spuid获取销售属性和销售属性值并且锁定
     */
    List<SpuSaleAttr> getSpuSaleAttrAndSaleAttrValues(@Param("skuId") Long skuId, @Param("spuId") Long spuId);
}
