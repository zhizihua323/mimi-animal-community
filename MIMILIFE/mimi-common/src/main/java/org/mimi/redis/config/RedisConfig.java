package org.mimi.redis.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@Configuration
@ConditionalOnClass(RedisTemplate.class)
public class RedisConfig {

    // 全局日期时间格式
    public static final String GLOBAL_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    // 全局日期格式
    public static final String GLOBAL_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 配置自定义ObjectMapper：
     * 1. 支持Java 8日期类型
     * 2. 保留类型信息（关键！解决LinkedHashMap转实体类问题）
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 1. 开启类型信息保留：在JSON中添加@class字段，存储类的全限定名
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance, // 类型验证器（兼容Jackson 2.10+）
                ObjectMapper.DefaultTyping.NON_FINAL,   // 对非final类保留类型信息（实体类通常非final）
                JsonTypeInfo.As.PROPERTY                // 类型信息以属性形式（@class）嵌入JSON
        );

        // 2. 注册Java 8时间模块，解决LocalDate/LocalDateTime序列化
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(GLOBAL_DATE_TIME_FORMAT)));
        javaTimeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern(GLOBAL_DATE_FORMAT)));
        objectMapper.registerModule(javaTimeModule);

        // 3. 禁用时间戳序列化，强制字符串格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 4. 设置时区（避免时区偏移）
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        return objectMapper;
    }

    /**
     * 配置RedisTemplate：使用带类型信息的ObjectMapper
     */
    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Serializable> redisTemplate(
            RedisConnectionFactory redisConnectionFactory,
            ObjectMapper redisObjectMapper) { // 注入上面配置的ObjectMapper
        System.out.println("redisTemplate初始化");
        RedisTemplate<String, Serializable> template = new RedisTemplate<>();

        // 配置连接工厂
        template.setConnectionFactory(redisConnectionFactory);

        // Key序列化：String类型（避免乱码）
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);

        // Value序列化：使用带类型信息的Jackson序列化器（关键！）
        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);

        // 初始化模板
        template.afterPropertiesSet();

        return template;
    }

    /**
     * 配置StringRedisTemplate（处理纯字符串数据，保持默认）
     */
    @Bean
    @ConditionalOnMissingBean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

}