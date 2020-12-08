package com.atguigu.gmall.weball.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yuanpf
 * @create 2020-12-08 20:04
 */
@Controller
public class IndexController {

    @Autowired
    ProductFeignClient productFeignClient;

    @RequestMapping({"/","index.html"})
    public String index(HttpServletRequest request){
        Result baseCategoryList = productFeignClient.getBaseCategoryList();
        request.setAttribute("list",baseCategoryList.getData());
        return "index/index";
    }
}
