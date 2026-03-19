package org.mimi.redis.config;

import org.mimi.redis.utils.RedisConstant;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomArchivalConfig {
    @Autowired
    private RedissonClient redisClient;

    @Bean
    public RBloomFilter<Long> goodsBloomFilter(){
        RBloomFilter<Long> bloomFilter = redisClient.getBloomFilter(RedisConstant.GOODS_BLOOM_FILTER);
        bloomFilter.tryInit(10000,0.001);
        return bloomFilter;
    }
}
