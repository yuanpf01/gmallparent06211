package com.atguigu.gmall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author yuanpf
 * @create 2020-11-30 11:05
 */
public interface ManageService {
    /**
     * 查询所有的一级分类的信息
     */
    public List<BaseCategory1> getCategory1();

    /**
     * 根据一级分类的id,查询一级分类下的二级分类信息
     */
    public List<BaseCategory2> getCategory2(Long category1Id);

    /**
     * 根据二级分类id ，查询二级分类下的三级分类信息
     */
    public List<BaseCategory3> getCategory3(Long category2Id);

    /**
     * 根据分类Id 获取平台属性数据
     * 接口说明：
     *      1，平台属性可以挂在一级分类、二级分类和三级分类
     *      2，查询一级分类下面的平台属性，传：category1Id，0，0；   取出该分类的平台属性
     *      3，查询二级分类下面的平台属性，传：category1Id，category2Id，0；
     *         取出对应一级分类下面的平台属性与二级分类对应的平台属性
     *      4，查询三级分类下面的平台属性，传：category1Id，category2Id，category3Id；
     *         取出对应一级分类、二级分类与三级分类对应的平台属性
     *
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);//保存平台属性

    List<BaseAttrValue> getAttrValueList(Long attrId);//根据id获得平台属性值

    /**
     * 根据平台属性id获取平台属性对象
     * @param attrId
     * @return
     */

    BaseAttrInfo getBaseAttrInfo(Long attrId);

    /**
     * 根据三级分类的id查询商品的spu信息
     * @param page1
     * @param spuInfo
     * @return
     */

    IPage<SpuInfo> getSpuInfoPageList(Page<SpuInfo> page1, SpuInfo spuInfo);

    /**
     * 获取品牌属性
     * 分页查询品牌列表信息
     * @param baseTrademarkPage
     * @return
     */

    IPage getBaseTrademarkPage(Page<BaseTrademark> baseTrademarkPage);
    /**
     * //根据skuid获取商品属性和图片
     */
    SkuInfo getSkuInfoBySkuId(Long skuId);
    /**
     * 需求分析：
     * 三级分类id。在skuinfo中提供
     * 根据三级分类id获取三级分类名称、二级分类id
     * 根据二级分类id获取二级分类名称、一级分类id
     * 根据一级分类id获取一级分类名称
     *具体实现：
     * 通过三级分类id查询一级分类、二级分类、三级分类的视图获取数据
     */
    public BaseCategoryView getBaseCategoryName(Long category3Id);

    /**
     * 根据skuid获取最新的价格
     */
    public BigDecimal getSkuPriceBySkuId(Long skuId);

    //获取首页显示的分类数据的方法
    public List<JSONObject> getBaseCategoryList();

    List<BaseAttrInfo> getBaseAttrInfoList(Long skuId);

    BaseTrademark selectTrademarkData(Long tmId);


}
