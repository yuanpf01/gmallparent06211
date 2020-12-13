package com.atguigu.gmall.client;

import com.atguigu.gmall.client.impl.ListDegradeFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author yuanpf
 * @create 2020-12-10 16:45
 */
@FeignClient(value = "service-list",fallback = ListDegradeFeignClient.class)
public interface ListFeignClient {

    /**
     * 更新热度排名
     */
    @GetMapping("api/list/inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable Long skuId);
     /**
     * 上架商品
     * @param skuId
     * @return
     */
    @GetMapping("/api/list/inner/upperGoods/{skuId}")
    Result upperGoods(@PathVariable("skuId") Long skuId);

    /**
     * 下架商品
     * @param skuId
     * @return
     */
    @GetMapping("/api/list/inner/lowerGoods/{skuId}")
    Result lowerGoods(@PathVariable("skuId") Long skuId);

    /**
     * 搜索商品
     * @param searchParam
     * @return
     */
    @PostMapping("/api/list")
    public Result list(@RequestBody SearchParam searchParam) throws Exception;
}
