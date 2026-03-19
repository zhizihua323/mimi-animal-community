package org.mimi.Interceptor;

import org.mimi.Interceptor.config.UserMessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserMessageInterceptor userMessageInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        System.out.println("userMessage拦截器生效");
        registry.addInterceptor(userMessageInterceptor)
                .addPathPatterns("/**")
                // 添加/** ，放行/user/wxLogin下面的所有请求
                .excludePathPatterns("/user/wxLogin/**");
    }
}
