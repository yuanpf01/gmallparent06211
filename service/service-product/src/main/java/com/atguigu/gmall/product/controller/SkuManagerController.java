package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author yuanpf
 * @create 2020-12-02 15:04
 */
@Api(tags = "sku后台数据接口")
@RestController
@RequestMapping("/admin/product/")
public class SkuManagerController {

    @Autowired
    private SkuInfoService skuInfoService;


    /**
     *
     * 添加sku
     * http://api.gmall.com/admin/product/saveSkuInfo
     * POST
     */
    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        skuInfoService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    /**
     * 获取sku分页列表
     * http://api.gmall.com/admin/product/list/{page}/{limit}
     *
     */
    @GetMapping("list/{page}/{limit}")
    public Result skuInfoPageList(
                                   @PathVariable Long page,
                                   @PathVariable Long limit
                                  ){

        Page<SkuInfo> skuInfoPage = new Page<>(page, limit);
        IPage<SkuInfo> skuInfoPageList = skuInfoService.getSkuInfoPageList(skuInfoPage);
        return Result.ok(skuInfoPageList);
    }
    /**
     * 上架
     * http://api.gmall.com/admin/product/onSale/33
     */
    @GetMapping("onSale/{skuId}")
    public Result SkuOnSale(@PathVariable Long skuId){
        skuInfoService.skuOnSaleService(skuId);
        return Result.ok();
    }
    /**
     * 下架
     * http://api.gmall.com/admin/product/cancelSale/29
     * Request Method: GET
     * Request Method: GET
     */
    @GetMapping("cancelSale/{skuId}")
    public Result skuCancelSale(@PathVariable Long skuId){
        skuInfoService.skuCancelSaleService(skuId);
        return Result.ok();
    }
}
