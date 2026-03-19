package com.example.coreshopping.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    public static final String GLOBAL_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public ObjectMapper objectMapper() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

        // 注册 Java 8 时间模块（包含 LocalDate、LocalDateTime 支持）
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // 为 LocalDateTime 设置自定义格式
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(GLOBAL_DATE_TIME_FORMAT)));
        // （补充）为 LocalDate 设置自定义格式（避免默认格式不一致）
        javaTimeModule.addSerializer(LocalDate.class,
                new com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        builder.modules(javaTimeModule);

        builder.timeZone(TimeZone.getTimeZone("GMT+8"));
        builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return builder.build();
    }
}