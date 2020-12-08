package com.atguigu.gmall.item.service;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author yuanpf
 * @create 2020-12-05 22:57
 */
public interface ItemService {

    /**
     * 根据skuid进行数据的整合
     * @param spuId
     * @return
     */
    Map<String,Object> getBySkuId(Long spuId);
}
