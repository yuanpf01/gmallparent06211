package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author yuanpf
 * @create 2020-12-06 18:09
 */
@RestController
@RequestMapping("api/item")
public class ItemController {

    @Autowired
    public ItemService itemService;

    /**
     * 调用product进行数据汇总
     * @param skuId
     * @return
     */
    @GetMapping("{skuId}")
    public Result getItemData(@PathVariable Long skuId){
        Map<String, Object> map = itemService.getBySkuId(skuId);
        return Result.ok(map);
    }
}
