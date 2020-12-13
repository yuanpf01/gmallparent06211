package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

/**
 * @author yuanpf
 * @create 2020-12-09 23:17
 */
public interface SearchService {
    public void upperGoods(Long skuId) ;//上架
    public void lowerGoods(Long skuId);//下架
    //更新热度排名
    public void incrHotScore(Long skuId);
    /**
     * 查询数据
     */
    SearchResponseVo search(SearchParam searchParam) throws Exception;
}
