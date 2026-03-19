package com.example.coreshopping.config;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FanoutRabbitConfig {


    @Bean
    public Queue queueA(){
        return new Queue("fanout.A");
    }
}
