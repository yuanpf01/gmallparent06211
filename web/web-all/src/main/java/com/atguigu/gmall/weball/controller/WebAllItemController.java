package com.atguigu.gmall.weball.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * skuAttrValueList==null
 * skuSaleAttrValueList==null
 * @author yuanpf
 * @create 2020-12-06 18:16
 */
@Controller
public class WebAllItemController {
    @Autowired
    private ItemClient itemClient;

    @RequestMapping("{skuId}.html")
    public String getItemDatas(@PathVariable Long skuId, Model model){
        Result<Map> result = itemClient.getItemData(skuId);
        model.addAllAttributes(result.getData());
        return "item/index";
    }
}
