package com.atguigu.gmall.list.service;

/**
 * @author yuanpf
 * @create 2020-12-09 23:17
 */
public interface SearchService {
    public void upperGoods(Long skuId) ;//上架
    public void lowerGoods(Long skuId);//下架
    //更新热度排名
    public void incrHotScore(Long skuId);
}
