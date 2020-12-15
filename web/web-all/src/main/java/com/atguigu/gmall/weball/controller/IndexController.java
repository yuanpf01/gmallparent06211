package com.atguigu.gmall.weball.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author yuanpf
 * @create 2020-12-08 20:04
 */
@Controller
public class IndexController {

    @Autowired
    ProductFeignClient productFeignClient;

//    @RequestMapping({"/","index.html"})
//    public String index(HttpServletRequest request){
//        Result baseCategoryList = productFeignClient.getBaseCategoryList();
//        request.setAttribute("list",baseCategoryList.getData());
//        return "index/index";
//    }
//      写控制器 /,index.html
        @RequestMapping("index.html")
        public String index(Model model){
        //  后台存储一个list
        Result result = productFeignClient.getBaseCategoryList();
        model.addAttribute("list",result.getData());
        //  返回页面
        return "index/index";
    }

        @RequestMapping("/")
        public String index1(Model model){
            Result result = productFeignClient.getBaseCategoryList();
            model.addAttribute("list",result.getData());
            //  返回页面
            return "index/index";
        }

    }
