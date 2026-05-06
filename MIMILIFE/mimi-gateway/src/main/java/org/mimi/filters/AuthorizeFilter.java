package org.mimi.filters;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

//定义顺序，越小优先级越高，再注入bean容器
@Order(-1)
@Component
public class AuthorizeFilter implements GlobalFilter {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //拦截器 新网关鉴权-能够区分“普通用户”和“管理员”
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求参数
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 直接放行：小程序的登录接口 和 管理员的登录接口
        if (path.startsWith("/user/wxLogin") || path.startsWith("/admin/sys/login")) {
            return chain.filter(exchange);
        }

        MultiValueMap<String,String> headers = request.getHeaders();
        //获取参数的authorization (你的代码里叫 token)
        String auth = headers.getFirst("token");

        if(auth == null || auth.isEmpty()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 2. 核心隔离逻辑：根据路径前缀去 Redis 里查不同的 Key
        if (path.startsWith("/admin/")) {
            // 如果是管理员接口，去查 admin:login: 前缀
            String admin = stringRedisTemplate.opsForValue().get("admin:login:" + auth);
            if(admin != null){
                return chain.filter(exchange);
            }
        } else {
            // 如果是原有的用户接口，去查 user:login: 前缀
            String user = stringRedisTemplate.opsForValue().get("user:login:" + auth);
            if(user != null){
                return chain.filter(exchange);
            }
        }

        // 验证失败拦截
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
    // *******************5.6修改-原本filter方法-未添加管理员端*************************
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        //获取请求参数
//        ServerHttpRequest request = exchange.getRequest();
//
//        String path = request.getURI().getPath();
//
//        // 对 /user/* 路径直接放行（不验证token）
//        if (path.startsWith("/user/wxLogin")) {
//            return chain.filter(exchange);
//        }
//
//        MultiValueMap<String,String> params = request.getQueryParams();
//        MultiValueMap<String,String> headers = request.getHeaders();
//        //获取参数的authorization
//        String auth = headers.getFirst("token");
//        //如果合法放行
//        String user =stringRedisTemplate.opsForValue().get("user:login:" + auth);
//        if(user != null){
//            return chain.filter(exchange);
//        }else {
//            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//            exchange.getResponse().setComplete();
//        }
//
//        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//        return exchange.getResponse().setComplete(); // 注意这里必须返回Mono，不能返回null
//    }
}
