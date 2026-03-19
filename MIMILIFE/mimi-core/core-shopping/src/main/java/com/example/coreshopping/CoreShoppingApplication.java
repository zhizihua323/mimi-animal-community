package com.example.coreshopping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"org.mimi.Interceptor","org.mimi.redis", "com.example.coreshopping"})
@EnableDiscoveryClient
@EnableFeignClients
public class CoreShoppingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreShoppingApplication.class, args);
    }

}
