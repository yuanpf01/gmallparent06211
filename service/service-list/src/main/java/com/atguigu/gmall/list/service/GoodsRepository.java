package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author yuanpf
 * @create 2020-12-10 11:26
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {

}
