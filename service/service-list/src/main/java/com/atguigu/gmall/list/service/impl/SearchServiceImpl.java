package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.service.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.significant.ParsedSignificantLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author yuanpf
 * @create 2020-12-09 23:18
 */
@Service
public class SearchServiceImpl  implements SearchService {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private GoodsRepository goodsRepository;

    //
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //上架
    @Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();
        //调用product ,根据skuid查询得到skuinfo + baseattr + baseattrvalue,封装到goods里面

        //异步编排
        CompletableFuture<SkuInfo> skuInfoAndImageCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //skuinfo数据和图片
            SkuInfo skuInfo = productFeignClient.getSkuInfoAndImageBySkuId(skuId);
            goods.setId(skuId);
            goods.setTitle(skuInfo.getSkuName());
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());
            goods.setPrice(skuInfo.getPrice().doubleValue());
            goods.setCreateTime(new Date());
            return skuInfo;
        });

        CompletableFuture<Void> baseCategoryViewcompletableFuture = skuInfoAndImageCompletableFuture.thenAcceptAsync((skuInfo) -> {
            //根据三级分类的id获取一级、二级、三级分类的id和名称
            BaseCategoryView baseCategoryViewList = productFeignClient.getBaseCategoryViewList(skuInfo.getCategory3Id());
            goods.setCategory1Id(baseCategoryViewList.getCategory1Id());
            goods.setCategory2Id(baseCategoryViewList.getCategory2Id());
            goods.setCategory3Id(baseCategoryViewList.getCategory3Id());
            goods.setCategory1Name(baseCategoryViewList.getCategory1Name());
            goods.setCategory2Name(baseCategoryViewList.getCategory2Name());
            goods.setCategory3Name(baseCategoryViewList.getCategory3Name());
        });


        CompletableFuture<Void> baseAttrInfoCompletableFuture = CompletableFuture.runAsync(() -> {
            //平台属性名 +平台属性值searAttrList
            List<BaseAttrInfo> baseAttrInfoList = productFeignClient.getBaseAttrInfo(skuId);
            /*List<SearchAttr> collect = baseAttrInfoList.stream().map((baseAttrInfo) -> {
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(baseAttrInfo.getId());
                searchAttr.setAttrName(baseAttrInfo.getAttrName());
                searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
//                searchAttrList.add(searchAttr);
                return searchAttr;
            }).collect(Collectors.toList());*/
            List<SearchAttr> searchAttrList = new ArrayList<>();
            baseAttrInfoList.stream().forEach((baseAttrInfo)->{
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(baseAttrInfo.getId());
                searchAttr.setAttrName(baseAttrInfo.getAttrName());
                searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
//              searchAttrList.add(searchAttr);
            });
            // goods.setAttrs(collect);
            goods.setAttrs(searchAttrList);
        });
        CompletableFuture<Void> trademarkDataCompletableFuture = skuInfoAndImageCompletableFuture.thenAcceptAsync((skuInfo) -> {
            //品牌数据
            BaseTrademark trademarkData = productFeignClient.getTrademarkData(skuInfo.getTmId());
            goods.setTmId(skuInfo.getTmId());
            goods.setTmName(trademarkData.getTmName());
            goods.setTmLogoUrl(trademarkData.getLogoUrl());
        });
        CompletableFuture.allOf(
                skuInfoAndImageCompletableFuture,
                baseCategoryViewcompletableFuture,
                baseAttrInfoCompletableFuture,
                trademarkDataCompletableFuture
        ).join();
        //保存
        this.goodsRepository.save(goods);
    }

    @Override//下架
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    //更新热度排名
    public void incrHotScore(Long skuId){
        //确定存储的数据类型--->zset
        String  hotScoreKey =  "hotScore";
        Double hotScore = redisTemplate.opsForZSet().incrementScore(hotScoreKey, "skuId:" + skuId,1);
        if (hotScore % 10 == 0){//判断
            Optional<Goods> optional = this.goodsRepository.findById(skuId);
            Goods goods = optional.get();
            goods.setHotScore(hotScore.longValue());
            this.goodsRepository.save(goods);
        }

    }

    @Override
    public SearchResponseVo search(SearchParam searchParam) throws IOException {
        //根据分类id、关键字等条件查询的所有字段封装在SearchParam类中，查询得到的所有数据封装在SearchResponseVo中
        /**
         * 获取动态生成的dsl语句
         * 执行dsl语句
         * 返回执行的结果
         */
        //buildQueryDsl(searchParam)方法动态生成dsl语句并将生成的记过放入searchRequest这个对象
        SearchRequest searchRequest = this.buildQueryDsl(searchParam);

        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //将dsl语句执行的结果转换为SearchResponseVo
        SearchResponseVo searchResponseVo = this.parseSearchResult(response);
        //赋值
        searchResponseVo.setPageNo(searchParam.getPageNo());
        searchResponseVo.setPageSize(searchParam.getPageSize());
        //总页数 = （总记录数 + 每页显示记录数 - 1）/每页显示记录数
        Long totalPages = (searchResponseVo.getTotal() + searchParam.getPageSize() - 1) / searchParam.getPageSize();
        searchResponseVo.setTotal(totalPages);
        return searchResponseVo;
    }

    /**
     * 执行dsl语句并将执行的结果转换为SearchResponseVo对象
     * @param response
     * @return
     */
    private SearchResponseVo parseSearchResult(SearchResponse response) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();//封装需要返回的数据
        //获取hits
        SearchHits hits = response.getHits();
        SearchHit[] hits1 = hits.getHits();

        //存放goods的集合
        ArrayList<Goods> goodsArrayList = new ArrayList<>();
        if (hits1 != null && hits1.length > 0){
            for (SearchHit subHit : hits1) {
                //获取到JSON字符串类型的数据
                String goodsSource = subHit.getSourceAsString();
                //类型转为为goos类型
                Goods goods = JSONObject.parseObject(goodsSource, Goods.class);
                //判断是否有高亮字段
                if (subHit.getHighlightFields().get("title") != null){//有高亮字段
                    Text titles = subHit.getHighlightFields().get("title").getFragments()[0];
                    //设置
                    goods.setTitle(titles.toString());
                }
                goodsArrayList.add(goods);
            }
        }

        //获取品牌数据  query -->aggs[attrs.id] -->attrname\attrvalue
        Map<String, Aggregation> tmIdMap = response.getAggregations().asMap();
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) tmIdMap.get("tmIdAgg");
        List<SearchResponseTmVo> tradmarklist = tmIdAgg.getBuckets().stream().map((bucket) -> {
            //声明一个品牌对象
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            //获取品牌id
            Number tmId = ((Terms.Bucket) bucket).getKeyAsNumber();
            searchResponseTmVo.setTmId(tmId.longValue());
            //获取品牌名称
            ParsedStringTerms tmNameAgg = ((Terms.Bucket) bucket).getAggregations().get("tmNameAgg");
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(tmName);
            //获取品牌logo
            ParsedStringTerms tmLogoUrlAgg = ((Terms.Bucket) bucket).getAggregations().get("tmLogoUrlAgg");
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
            return searchResponseTmVo;
        }).collect(Collectors.toList());
        //将品牌数据放入返回的对象里面
        searchResponseVo.setTrademarkList(tradmarklist);

        //获取平台属性attr ->aggs[attrid]---->aggs[attrname attrvalue]
        ParsedNested attrAgg = (ParsedNested) tmIdMap.get("attrAgg");

        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");

        List<SearchResponseAttrVo> attrlist = attrIdAgg.getBuckets().stream().map((bucket) -> {
            //平台属性对象
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            Number attrId = ((Terms.Bucket) bucket).getKeyAsNumber();
            searchResponseAttrVo.setAttrId(attrId.longValue());
            ParsedStringTerms attrNameAgg = ((Terms.Bucket) bucket).getAggregations().get("attrNameAgg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseAttrVo.setAttrName(attrName);
            ParsedStringTerms attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
            List<? extends Terms.Bucket> buckets = attrValueAgg.getBuckets();
            searchResponseAttrVo.setAttrValueList(buckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList()));
            return searchResponseAttrVo;
        }).collect(Collectors.toList());
        //赋值
        searchResponseVo.setAttrsList(attrlist);
        return searchResponseVo;
    }

    /**
     * 根据查询的条件：关键字，平台属性、排序、分类等动态生成dsl语句并封装到searchrequest这个对象里面
     * @param searchParam
     * @return
     */
    private SearchRequest buildQueryDsl(SearchParam searchParam) {//动态生成dsl语句并执行
        //构建一个查询器  相当于dsl语句中的{}
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //bool
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //判断是否是根据分类id进行查询
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())){//非空，根据分类id进行查询
           boolQuery.filter(QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id()));
        }
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())){//非空，根据分类id进行查询
           boolQuery.filter(QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id()));
        }
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())){//非空，根据分类id进行查询
           boolQuery.filter(QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id()));
        }
        //判断是否根据关键词检索
        if (!StringUtils.isEmpty(searchParam.getKeyword())){
            boolQuery.must(QueryBuilders.matchQuery("title",searchParam.getKeyword()).operator(Operator.AND));
        }
        //判断是否根据品牌进行检索
        if (!StringUtils.isEmpty(searchParam.getTrademark())){
            //2:华为
//            String[] split1 = org.apache.commons.lang3.StringUtils.split(searchParam.getTrademark());
            String[] split = searchParam.getTrademark().split(":");
            if (split != null && split.length >0){
                boolQuery.filter(QueryBuilders.termQuery("tmId",split[0]));
            }
        }
        //GET goods/info/_search {   "query": {     query语句执行
        searchSourceBuilder.query(boolQuery);
        //设置分页   from size  和query并行
        //从第from条记录开始
        int from = (searchParam.getPageNo() - 1) * searchParam.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(searchParam.getPageSize());

        //高亮显示，和query同一级别
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");//高亮显示的字段
        highlightBuilder.preTags("<span style=color:red>");//前缀
        highlightBuilder.postTags("</span>");//后缀
        //设置
        searchSourceBuilder.highlighter(highlightBuilder);

        //排序
        //传递的参数格式 1：desc ,2:asc   1代表hotscore  2代表price
        //得到排序的字段
        String order = searchParam.getOrder();
        if (StringUtils.isEmpty(order)){
            //拆分为数组
            String[] split = org.apache.commons.lang3.StringUtils.split(order);
            if (split != null && split.length > 0){
                //生命一个用来排序的字段
                String field = null;
                switch (split[0]){//split[0] = 1 或 2
                    case "1" :
                       field = "hotScore";//根据热度排序【访问量】
                       break;
                    case "2":
                        field = "price"; //根据价格排序
                        break;
                }
                searchSourceBuilder.sort(field,"asc".equals(split[1])? SortOrder.ASC: SortOrder.DESC);
            }else {//没有排序的字段和值
                searchSourceBuilder.sort("hotScore", SortOrder.DESC);//默认依照热度排序
            }
        }else {
            searchSourceBuilder.sort("hotScore", SortOrder.DESC);//默认依照热度排序
        }

        //平台属性值过滤
        //获取平台属性  23:4G:运行内存    id+值+名称
        String[] props = searchParam.getProps();
        if (!StringUtils.isEmpty(props)) {
            for (String prop : props) {
                String[] split = prop.split(":");
                if (split != null && split.length == 3){
                    //外层
                    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                    //内层
                    BoolQueryBuilder boolQueryBuilder1 = QueryBuilders.boolQuery();
                    //首先根据里面的条件进行过滤
                    boolQueryBuilder1.must(QueryBuilders.termQuery("attrs.attrId",split[0]));
                    boolQueryBuilder1.must(QueryBuilders.termQuery("attrs.attrValue",split[1]));
                    //外层过滤 attrs  nested类型
                    boolQueryBuilder.must(QueryBuilders.nestedQuery("attrs",boolQueryBuilder1, ScoreMode.None));

                    //将attrs属性过滤放进query——>bool--->filter--->attrs--->attrid\attrvalue下面
                    boolQuery.filter(boolQueryBuilder);
                }
            }
        }

        //聚合
        //①聚合品牌
        searchSourceBuilder.aggregation(AggregationBuilders.terms("tmIdAgg").field("tmId")
                            .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                            .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl")));
        //②聚合平台属性  属性值  attrs类型 nested
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg","attrs")
                            .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                            .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                            .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));

        //设置需要返回的数据都有哪些
        searchSourceBuilder.fetchSource(new String[]{"id","defaultImg","title","price"},null);
        //设置查询的数据  get /goods/info/_search
        SearchRequest searchRequest = new SearchRequest("goods");
        searchRequest.searchType("info");
        //将动态生成的dsl语句放进searchRequest对象里面并返回
        searchRequest.source(searchSourceBuilder);//

        return searchRequest;
    }
}
