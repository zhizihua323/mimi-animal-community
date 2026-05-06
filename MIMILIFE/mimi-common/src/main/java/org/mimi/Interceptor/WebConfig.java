package org.mimi.Interceptor;

import org.mimi.Interceptor.config.AdminMessageInterceptor;
import org.mimi.Interceptor.config.UserMessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserMessageInterceptor userMessageInterceptor;

    @Autowired
    private AdminMessageInterceptor adminMessageInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        System.out.println("双端拦截器生效");

        // 1. 原有的小程序用户拦截器
        registry.addInterceptor(userMessageInterceptor)
                .addPathPatterns("/**")
                // 重点：必须在这里加上 /admin/**，让用户拦截器不要去管管理员的接口！
                .excludePathPatterns("/user/wxLogin/**", "/admin/**");

        // 2. 新增的管理员拦截器
        registry.addInterceptor(adminMessageInterceptor)
                .addPathPatterns("/admin/**") // 只拦截管理员路径
                .excludePathPatterns("/admin/sys/login"); // 放行管理员登录接口
    }
}

// ************5.6-原有拦截规则**************
//package org.mimi.Interceptor;
//
//import org.mimi.Interceptor.config.UserMessageInterceptor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//    @Autowired
//    private UserMessageInterceptor userMessageInterceptor;
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        System.out.println("userMessage拦截器生效");
//        registry.addInterceptor(userMessageInterceptor)
//                .addPathPatterns("/**")
//                // 添加/** ，放行/user/wxLogin下面的所有请求
//                .excludePathPatterns("/user/wxLogin/**");
//    }
//}
