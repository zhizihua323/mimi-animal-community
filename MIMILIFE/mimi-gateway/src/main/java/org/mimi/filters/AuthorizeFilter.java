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
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求参数
        ServerHttpRequest request = exchange.getRequest();

        String path = request.getURI().getPath();

        // 对 /user/* 路径直接放行（不验证token）
        if (path.startsWith("/user/wxLogin")) {
            return chain.filter(exchange);
        }

        MultiValueMap<String,String> params = request.getQueryParams();
        MultiValueMap<String,String> headers = request.getHeaders();
        //获取参数的authorization
        String auth = headers.getFirst("token");
        //如果合法放行
        String user =stringRedisTemplate.opsForValue().get("user:login:" + auth);
        if(user != null){
            return chain.filter(exchange);
        }else {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().setComplete();
        }

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete(); // 注意这里必须返回Mono，不能返回null
    }
}
