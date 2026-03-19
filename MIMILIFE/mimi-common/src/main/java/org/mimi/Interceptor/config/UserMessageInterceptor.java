package org.mimi.Interceptor.config;

import org.mimi.entity.User;
import org.mimi.redis.utils.RedisConstant;
import org.mimi.threadLocal.UserContextThreadLocal;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.servlet.HandlerInterceptor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.logging.Logger;


@Component
public class UserMessageInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        User user = (User) redisTemplate.opsForValue().get(RedisConstant.USER_LOGIN_KEY_PREFIX + token);
        if (user != null) {
            UserContextThreadLocal.setUser(user); // 存入ThreadLocal
        } else {
            // 可以根据需求处理未登录情况，比如返回401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContextThreadLocal.clear();
    }
}
//@Component
//public class UserMessageInterceptor implements WebFilter {
////    private static final Logger log = LoggerFactory.getLogger(UserMessageFilter.class);
//
//    // WebFlux 必须使用响应式 Redis 模板，避免阻塞
//    @Autowired
//    private RedisTemplate<String, Serializable> redisTemplate;
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//        // 1. 无 Token 直接放行（符合需求）
//        String token = exchange.getRequest().getHeaders().getFirst("token");
//        if (token == null || token.trim().isEmpty()) {
////            log.debug("[用户过滤器] 无 Token，直接放行，路径：{}", exchange.getRequest().getPath());
//            return chain.filter(exchange);
//        }
//
//        // 2. 有 Token：用异步线程池执行 Redis 阻塞操作（关键：避免阻塞 WebFlux 事件循环）
//        String redisKey = RedisConstant.USER_LOGIN_KEY_PREFIX + token;
//        return Mono.fromCallable(() -> {
//                    // 3. 异步执行 Redis 查询（阻塞操作放在独立线程）
//                    Serializable value = redisTemplate.opsForValue().get(redisKey);
//                    return value instanceof User ? (User) value : null;
//                })
//                .subscribeOn(Schedulers.boundedElastic()) // 绑定弹性线程池（专门处理阻塞任务）
//                .flatMap(user -> {
//                    // 4. Token 有效：存储用户信息，后续清除 ThreadLocal
//                    if (user != null) {
//                        UserContextThreadLocal.setUser(user);
////                        log.debug("[用户过滤器] Token 有效，用户ID：{}，路径：{}",
////                                user.getId(), exchange.getRequest().getPath());
//                        return chain.filter(exchange)
//                                .doFinally(signalType -> {
//                                    UserContextThreadLocal.clear(); // 防止内存泄漏
////                                    log.debug("[用户过滤器] 清除 ThreadLocal，路径：{}",
////                                            exchange.getRequest().getPath());
//                                });
//                    }
//                    // 5. Token 无效：返回 401
////                    log.warn("[用户过滤器] Token 无效，RedisKey：{}，路径：{}",
////                            redisKey, exchange.getRequest().getPath());
//                    return handleUnauthorized(exchange, "Token 无效或已过期，请重新登录");
//                })
//                .onErrorResume(e -> {
//                    // 6. 异常处理（如 Redis 故障）
////                    log.error("[用户过滤器] Redis 校验异常，RedisKey：{}", redisKey, e);
//                    return handleUnauthorized(exchange, "认证服务临时故障，请稍后重试");
//                });
//    }
//
//
//    /**
//     * 统一处理未授权响应（仅在 Token 无效/异常时触发）
//     */
//    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
//        // 设置 401 状态码和 JSON 响应格式
//        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//        exchange.getResponse().getHeaders().add(
//                "Content-Type", "application/json;charset=UTF-8"
//        );
//
//        // 构建 JSON 响应体（前端可统一解析 code/message）
//        String jsonResponse = String.format("{\"code\":401,\"message\":\"%s\",\"data\":null}", message);
//        byte[] responseBytes = jsonResponse.getBytes();
//
//        // 响应写入（WebFlux 响应式写入方式）
//        return exchange.getResponse()
//                .writeWith(Mono.just(
//                        exchange.getResponse().bufferFactory().wrap(responseBytes)
//                ));
//    }
//}

