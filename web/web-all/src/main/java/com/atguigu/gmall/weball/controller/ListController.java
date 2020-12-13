package com.atguigu.gmall.weball.controller;

import com.atguigu.gmall.client.ListFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// http://list.gmall.com/list.html?category3Id=61  http://list.gmall.com/list.html?keyword=小米手机

@Controller
public class ListController {

    @Autowired
    private ListFeignClient listFeignClient;

    /**
     * 全文搜索
     * @param searchParam
     * @return
     */
    @GetMapping("list.html")
    public String search(SearchParam searchParam, Model model) throws Exception{
        //根据用户的检索条件，生成dsl语句，执行后获取数据
        Result<Map> result = listFeignClient.list(searchParam);
//        ${urlParam}+'&trademark='+${trademark.tmId}+':'+${trademark.tmName}+'&order='+${searchParam.order
        //记录查询条件 ：
        String urlParam = this.makeUrlParam(searchParam);
        //<a th:href="@{${#strings.replace(urlParam+'&order='+searchParam.order,'props='+prop.attrId+':'+prop.attrValue+':'+prop.attrName,'')}}">

        //存储品牌数据${trademarkParam}
        String trademarkParam = this.makeTrademarkParam(searchParam.getTrademark());
        //${propsParamList}存储平台属性的面包屑
        List<Map<String,Object>>  propsParamList = this.makepropsParamList(searchParam.getProps());

        //  获取排序规则
        //  ${orderMap.type == '1' ? 'active': ''}  ${orderMap.sort == 'asc' ? 'desc' : 'asc'}
        HashMap<String,String> orderMap = this.dealOrder(searchParam.getOrder());
        model.addAttribute("orderMap",orderMap);
        model.addAttribute("propsParamList",propsParamList);
        model.addAttribute("trademarkParam",trademarkParam);
        model.addAttribute("urlParam",urlParam);
        //  存储数据
        model.addAllAttributes(result.getData());

        return "list/index";
    }
    //  第一次点击 ：order=1:asc  第二次点击 order=1:desc
    private HashMap<String, String> dealOrder(String order) {
        HashMap<String, String> hashMap = new HashMap<>();
        //  判断
        if(!StringUtils.isEmpty(order)){
            //  分割字符串
            String[] split = order.split(":");
            if (split!=null && split.length==2){
                hashMap.put("type",split[0]);
                hashMap.put("sort",split[1]);
            }else {
                hashMap.put("type","1");
                hashMap.put("sort","desc");
            }
        }else {
            hashMap.put("type","1");
            hashMap.put("sort","desc");
        }
        return hashMap;
    }

    //获取平台属性的面包屑
    private List<Map<String, Object>> makepropsParamList(String[] props) {

        List<Map<String, Object>> list = new ArrayList<>();
        //  &props=106:安卓手机:手机一级&props=107:小米:二级手机
        if (props!=null && props.length>0){
            //  循环遍历
            for (String prop : props) {
                //  每个prop = 106:安卓手机:手机一级
                //  获取平台属性名，平台属性值名，平台属性Id
                String[] split = prop.split(":");
                if (split!=null && split.length==3){
                    Map<String, Object> map = new HashMap<>();
                    map.put("attrId",split[0]);
                    map.put("attrValue",split[1]);
                    map.put("attrName",split[2]);
                    //  将map 添加到集合
                    list.add(map);
                }
            }
        }
        return list;
    }

    //获取品牌的面包屑
    private String makeTrademarkParam(String trademark) {
        //  &trademark=2:苹果
        if (!StringUtils.isEmpty(trademark)){
            //  字符串分割
            String[] split = org.apache.commons.lang3.StringUtils.split(trademark);
//方式二：推荐使用  String[] split = trademark.split(":");
            //  判断数据
            if (split!=null && split.length==2){
                //  返回品牌名称
                return "品牌:" + split[1];
            }
        }
        return null;
    }

    private String makeUrlParam(SearchParam searchParam) {
        StringBuilder urlParam = new StringBuilder();

        // 判断一级分类
        if (searchParam.getCategory1Id() != null) {
            urlParam.append("category1Id=").append(searchParam.getCategory1Id());
        }
        // 判断二级分类
        if (searchParam.getCategory2Id() != null) {
            urlParam.append("category2Id=").append(searchParam.getCategory2Id());
        }
        // 判断三级分类
        if (searchParam.getCategory3Id() != null) {
            urlParam.append("category3Id=").append(searchParam.getCategory3Id());
        }
        // 判断关键字
        if (searchParam.getKeyword() != null) {
            urlParam.append("keyword=").append(searchParam.getKeyword());
        }

        // 处理品牌
        if (searchParam.getTrademark() != null) {
            if (urlParam.length() > 0) {
                urlParam.append("&trademark=").append(searchParam.getTrademark());
            }
        }

        if (searchParam != null) {
            String[] props = searchParam.getProps();
            if (props != null) {
                if (props.length > 0){
                    for (String prop : props) {
                        if (urlParam.length() > 0) {
                            urlParam.append("&props=").append(prop);
                        }
                    }
            }
        }
    }

        return "list.html?" + urlParam.toString();
    }
}

