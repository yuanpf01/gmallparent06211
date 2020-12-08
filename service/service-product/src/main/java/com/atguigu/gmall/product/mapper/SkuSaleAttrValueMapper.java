package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author yuanpf
 * @create 2020-12-02 16:27
 */
@Mapper
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {
    /**
     * 根据spuId 获取spu对应的销售属性值组成的skuId 集合。
     * @param spuId
     * @return
     */
    List<Map> selectSkuSaleValueIdsMap(Long spuId);
}
