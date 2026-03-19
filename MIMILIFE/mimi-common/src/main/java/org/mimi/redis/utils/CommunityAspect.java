package org.mimi.redis.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.mimi.result.Result;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class CommunityAspect {

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(org.mimi.redis.utils.CommunityCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] methodParams = joinPoint.getArgs();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        CommunityCache communityCache = method.getAnnotation(CommunityCache.class);
        String prefix = communityCache.value();
        Object firstParam = methodParams[0];

        String cacheKey = prefix + firstParam;

        Object redisObject = redisTemplate.opsForValue().get(cacheKey);

        if(redisObject != null){
            return Result.ok(redisObject);
        }

        RLock lock = redissonClient.getLock("lock:" + firstParam);

        try{
            boolean isLocked = lock.tryLock(1, TimeUnit.MINUTES);

            if(isLocked){
                redisObject = redisTemplate.opsForValue().get(cacheKey);
                if(redisObject !=null){
                    return Result.ok(redisObject);
                }

                Object objectDb = joinPoint.proceed();
                if(objectDb != null){
                    redisTemplate.opsForValue().set(cacheKey, (Serializable) objectDb,3600,TimeUnit.SECONDS);
                    return Result.ok(objectDb);
                }
                return retryGetData(cacheKey, 3);

            }

        }catch (Exception error){
            Thread.currentThread().interrupt();
            return Result.error("服务器繁满，请重新尝试");
        }finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return joinPoint.proceed();
    }

    private Object retryGetData(String key, int maxRetries) throws InterruptedException {
        int retryCount = 0;
        while (retryCount < maxRetries) {
            // 随机等待一段时间，避免惊群效应
            TimeUnit.MILLISECONDS.sleep(50 + (int)(Math.random() * 100));

            Object data = redisTemplate.opsForValue().get(key);
            if (data != null) {
                return Result.ok(data);
            }
            retryCount++;
        }
        return Result.error(null);
    }
}
