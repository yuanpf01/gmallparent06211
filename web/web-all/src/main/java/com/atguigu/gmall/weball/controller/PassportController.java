package com.atguigu.gmall.weball.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yuanpf
 * @create 2020-12-14 20:48
 */
@Controller
public class PassportController {


    /**
     * 跳转到登录页面
     *
     */
    @GetMapping("login.html")
    public String login(HttpServletRequest request){

        //获取用户从哪里点击登录的url,用于登录成功后跳转的url------>originUrl
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl",originUrl);
        return "login";//返回登录页面
    }
}
