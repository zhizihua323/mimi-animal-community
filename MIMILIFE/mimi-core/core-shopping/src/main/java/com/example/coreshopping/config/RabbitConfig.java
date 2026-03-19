package com.example.coreshopping.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.MimeTypeUtils;

import javax.annotation.PostConstruct;

@Configuration
@Slf4j
public class RabbitConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 配置JSON消息转换器，解决序列化问题
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        // 创建Jackson对象映射器
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册JDK8时间模块，支持LocalDateTime等时间类型
        objectMapper.registerModule(new JavaTimeModule());
        // 禁用将日期序列化为时间戳的默认行为
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 创建并返回JSON消息转换器
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        // 设置默认内容类型为JSON

        return converter;
    }

    @PostConstruct
    public void initRabbitMQ() {
        log.info("rabbitMq初始化开始");

        // 设置消息转换器（解决核心序列化问题）
        rabbitTemplate.setMessageConverter(jsonMessageConverter());

        // 设置确认回调和返回回调
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);

        // 设置mandatory=true，确保returnCallback能收到不可路由的消息
        rabbitTemplate.setMandatory(true);

        log.info("rabbitMq初始化完成，已配置JSON转换器和消息回调");
    }

    /**
     * 消息发送到交换机的确认回调
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        String messageId = correlationData != null ? correlationData.getId() : "未知ID";
        if (ack) {
            log.info("消息[{}]已成功发送到交换机", messageId);
        } else {
            log.error("消息[{}]发送到交换机失败，原因：{}", messageId, cause);
            // 这里可以添加消息重发逻辑
        }
    }

    /**
     * 消息无法路由到队列时的回调
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        String messageId = message.getMessageProperties().getMessageId();
        log.error(
                "消息[{}]路由失败 - 交换机: {}, 路由键: {}, 响应码: {}, 原因: {}",
                messageId, exchange, routingKey, replyCode, replyText
        );
        // 这里可以添加失败处理逻辑，如转发到备用队列
    }
}
