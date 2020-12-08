package com.atguigu.gmall.item.fallback;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemClient;
import org.springframework.stereotype.Component;

/**
 * @author yuanpf
 * @create 2020-12-06 18:06
 */
@Component
public class ItemDegradeFeignClient implements ItemClient {

    @Override
    public Result getItemData(Long skuId) {
        return Result.fail();
    }
}
