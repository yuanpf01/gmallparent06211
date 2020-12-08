package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.cache.MyGmallCache;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.product.SpuSaleAttrValue;
import com.atguigu.gmall.product.mapper.SpuImageMapper;
import com.atguigu.gmall.product.mapper.SpuInfoMapper;
import com.atguigu.gmall.product.mapper.SpuSaleAttrMapper;
import com.atguigu.gmall.product.mapper.SpuSaleAttrValueMapper;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author yuanpf
 * @create 2020-12-01 23:54
 */
@Service
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoMapper, SpuInfo> implements SpuInfoService {

    @Autowired
    SpuImageMapper spuImageMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    SpuInfoMapper spuInfoMapper;

    /**
     * 添加spuifo信息【】
     * @param spuInfo
     */
    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {
        spuInfoMapper.insert(spuInfo);
        // 商品的图片集合
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (!CollectionUtils.isEmpty(spuImageList)){
            for (SpuImage spuImage : spuImageList) {
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insert(spuImage);
            }
            // 销售属性集合
            List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
            if (!CollectionUtils.isEmpty(spuSaleAttrList)){
                for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                    spuSaleAttr.setSpuId(spuInfo.getId());
                    //添加销售属性
                    spuSaleAttrMapper.insert(spuSaleAttr);
                    // 销售属性值对象集合
                    List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                    if (!CollectionUtils.isEmpty(spuSaleAttrValueList)){
                        for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                            spuSaleAttrValue.setSpuId(spuInfo.getId());
//                            spuSaleAttrValue.setBaseSaleAttrId(spuSaleAttr.getId());
                            //添加销售属性值
                            spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                        }
                    }
                }
            }
        }
    }
    /**
     * 根据spuid获取spu消费属性
     * @param spuId
     * @return
     */

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        QueryWrapper<SpuSaleAttr> spuSaleAttrQueryWrapper = new QueryWrapper<>();
        spuSaleAttrQueryWrapper.eq("base_sale_attr_id",spuId);
        spuSaleAttrQueryWrapper.orderByDesc("id");
        return spuSaleAttrMapper.selectList(spuSaleAttrQueryWrapper);
    }
    /**
     * 根据spuid获取图片列表
     * http://api.gmall.com/admin/product/spuImageList/{spuId}
     *
     */

    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        QueryWrapper<SpuImage> spuImageQueryWrapper = new QueryWrapper<>();
        spuImageQueryWrapper.eq("spu_id",spuId);
        spuImageQueryWrapper.orderByDesc("id");
        return spuImageMapper.selectList(spuImageQueryWrapper);
    }

    /**
     * 根据skuid、spuid获取销售属性值并锁定
     * @param skuId
     * @param spuId
     * @return
     */
    @Override
    @MyGmallCache(prefix = "SpuSaleAttrAndValues")
    public List<SpuSaleAttr> getSpuSaleAttrAndValues(Long skuId, Long spuId) {

        return spuSaleAttrMapper.getSpuSaleAttrAndSaleAttrValues(skuId,spuId);
    }
}
