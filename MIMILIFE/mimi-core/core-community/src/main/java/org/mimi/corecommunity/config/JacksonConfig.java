package org.mimi.corecommunity.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    // 1. 定义全局统一的日期时间格式（与前端匹配）
    public static final String GLOBAL_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // 2. 配置 ObjectMapper（Jackson 核心解析器）
    @Bean
    public ObjectMapper objectMapper() {
        // 初始化 Jackson 构建器
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

        // 3. 注册 Java 8 时间模块（解决 LocalDateTime 序列化问题）
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // 为 LocalDateTime 单独设置序列化格式（覆盖模块默认格式）
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(GLOBAL_DATE_TIME_FORMAT)));
        builder.modules(javaTimeModule);

        // 4. 设置全局时区（避免时间偏移，必须与数据库时区一致）
        builder.timeZone(TimeZone.getTimeZone("GMT+8"));

        // 5. 禁用时间戳格式（确保返回字符串格式，而非数字）
        builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 6. 构建 ObjectMapper 并返回
        return builder.build();
    }
}