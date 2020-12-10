package com.atguigu.gmall.client.impl;

import com.atguigu.gmall.client.ListFeignClient;
import com.atguigu.gmall.common.result.Result;
import org.springframework.stereotype.Component;

/**
 * @author yuanpf
 * @create 2020-12-10 16:47
 */
@Component
public class ListDegradeFeignClient implements ListFeignClient {
    @Override
    public Result incrHotScore(Long skuId) {
        return null;
    }
}
