package org.mimi.rabbitMq.utils;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.core.Message;
import com.rabbitmq.client.Channel;
import org.springframework.stereotype.Component;

@Component
public class FanoutConsumer {

    // 原队列和交换机
    public static final String QUEUE_NAME = "fanout_queue";
    public static final String EXCHANGE_FANOUT = "exchange_fanout";

    // 死信队列和死信交换机
    public static final String DEAD_LETTER_QUEUE = "dead_letter_queue";
    public static final String DEAD_LETTER_EXCHANGE = "dead_letter_exchange";
    public static final String DEAD_LETTER_ROUTING_KEY = "dead.key";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    value = QUEUE_NAME,
                    durable = "true",
                    arguments = {
                            // 指定死信交换机
                            @Argument(name = "x-dead-letter-exchange", value = DEAD_LETTER_EXCHANGE),
                            // 指定死信路由键
                            @Argument(name = "x-dead-letter-routing-key", value = DEAD_LETTER_ROUTING_KEY),
                            // 可选：设置队列消息过期时间（毫秒）
                            @Argument(name = "x-message-ttl", value = "10000", type = "java.lang.Long"),
                            // 可选：设置队列最大长度
                            @Argument(name = "x-max-length", value = "1000", type = "java.lang.Long")
                    }
            ),
            exchange = @Exchange(
                    value = EXCHANGE_FANOUT,
                    type = ExchangeTypes.FANOUT,
                    durable = "true"
            )
    ))
    public void processMessageBack(String dataString, Message message, Channel channel) {
        System.out.println("接收到消息: " + dataString);
        // 如果这里不手动确认消息且设置了requeue=false，消息会成为死信
    }

    // 死信队列的消费者，处理死信消息
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = DEAD_LETTER_QUEUE, durable = "true"),
            exchange = @Exchange(value = DEAD_LETTER_EXCHANGE, type = ExchangeTypes.DIRECT, durable = "true"),
            key = DEAD_LETTER_ROUTING_KEY
    ))
    public void processDeadLetter(String dataString, Message message, Channel channel) {
        System.out.println("接收到死信消息: " + dataString);
        // 处理死信消息逻辑
    }
}