package org.mimi.redis.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.TransportMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    //TODO
    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
//        config.setTransportMode(TransportMode.EPOLL);
        config.useSingleServer()
                .setAddress("redis://192.168.119.128:6389")
                .setPassword("123456");
        return Redisson.create(config);
    }

}
