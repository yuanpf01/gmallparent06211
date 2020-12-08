package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @author yuanpf
 * @create 2020-11-30 18:53
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter(){
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");//所有请求头信息  * 表示任意
        configuration.addAllowedMethod("*");//设置允许访问的额方法
        configuration.addAllowedOrigin("*");//设置允许访问的网络
        configuration.setAllowCredentials(true);//设置是否从服务器获取cookie

        //配置资源对象
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        /**
         * path:
         *
         */
        configurationSource.registerCorsConfiguration("/**",configuration);
        return new CorsWebFilter(configurationSource);
    }
}
