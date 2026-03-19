package org.mimi.config;
import feign.Logger;
import org.springframework.context.annotation.Bean;


public class DefaultFeignConfiguration {
    /**
     * feign 日志级别
     * @return
     */
    @Bean
    public Logger.Level logLever(){
        return  Logger.Level.BASIC;
    }
}
