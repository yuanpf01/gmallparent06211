package com.atguigu.gmall.item.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.fallback.ItemDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author yuanpf
 * @create 2020-12-06 18:04
 */
@FeignClient(name = "service-item",fallback = ItemDegradeFeignClient.class)
public interface ItemClient {

    @GetMapping("/api/item/{skuId}")
    public Result getItemData(@PathVariable("skuId") Long skuId);
}
