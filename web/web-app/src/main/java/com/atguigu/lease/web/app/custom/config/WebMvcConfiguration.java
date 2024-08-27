package com.atguigu.lease.web.app.custom.config;

import com.atguigu.lease.web.app.custom.converter.StringToBaseEnumConverterFactory;
import com.atguigu.lease.web.app.custom.interceptor.AuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//WebMvcConfigurer是一种常用的配置方式
//可以允许我们自定义Spring MVC的行为，比如添加拦截器、消息转换器等。
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;
    @Autowired
    private StringToBaseEnumConverterFactory stringToBaseEnumConverterFactory;


    //注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.authenticationInterceptor).addPathPatterns("/app/**").excludePathPatterns("/app/login/**");
        //拦截所有app/下的请求，除了app/login登录请求
    }
    //注册字符串到枚举类的转换器
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(this.stringToBaseEnumConverterFactory);
    }
}
