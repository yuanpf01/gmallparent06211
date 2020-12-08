package com.atguigu.gmall.weball;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

/**
 * @author yuanpf
 * @create 2020-12-06 17:55
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)//取消数据库的自动配置
@EnableFeignClients("com.atguigu.gmall")//远程调用
@EnableDiscoveryClient//服务注册与发现
@ComponentScan(basePackages = {"com.atguigu.gmall"})//组件扫描
public class WebAllApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebAllApplication.class,args);
    }
}
