package com.atguigu.gmall.client.impl;

import com.atguigu.gmall.client.ListFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.stereotype.Component;

/**
 * @author yuanpf
 * @create 2020-12-10 16:47
 */
@Component
public class ListDegradeFeignClient implements ListFeignClient {
    /**
     * 更新热度排名
     */
    @Override
    public Result incrHotScore(Long skuId) {
        return null;
    }
    /**
     * 上架商品
     * @param skuId
     * @return
     */
    @Override
    public Result upperGoods(Long skuId) {
        return null;
    }
    /**
     * 下架商品
     * @param skuId
     * @return
     */
    @Override
    public Result lowerGoods(Long skuId) {
        return null;
    }

    /**
     * 搜索商品
     * @param searchParam
     * @return
     */
    @Override
    public Result list(SearchParam searchParam) throws Exception {
        return null;
    }
}
