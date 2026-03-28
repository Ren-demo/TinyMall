package com.mall.tinymall.config;

import com.mall.tinymall.inerceptor.LoginCheckInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class webConifg implements WebMvcConfigurer {
    @Autowired
    private LoginCheckInterceptor loginCheckInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
                //.excludePathPatterns("/**"); //测试时放行所有路径
                .addPathPatterns("/**") // 拦截所有路径
                .excludePathPatterns(
                        "/error",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/swagger-ui/**",
                        "/favicon.ico",
                        "/tinymall/login/**",
                        "/tinymall/mall/**",
                        "/tinymall/store/**",
                        "/tinymall/goods/**",
                        "/ai/**"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 所有接口
                .allowedOriginPatterns("*")  // Spring Boot 2.4.0+ 使用这个
                .allowedMethods("*")  // 所有请求方法
                .allowedHeaders("*")  // 所有请求头
                .exposedHeaders("*")  // 暴露所有响应头
                .allowCredentials(false)  // 注意：使用 "*" 时不能为 true
                .maxAge(3600);  // 预检请求缓存时间
    }
}