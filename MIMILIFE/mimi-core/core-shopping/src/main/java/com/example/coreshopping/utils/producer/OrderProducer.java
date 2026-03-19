package com.example.coreshopping.utils.producer;


import com.example.coreshopping.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.mimi.shopping.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static java.lang.Thread.sleep;

@Component
public class OrderProducer {

    @Autowired
    private OrderService orderService;
    private static final Logger log = LoggerFactory.getLogger(OrderProducer.class); // 替换为实际类名

    public static final String QUEUE_NAME = "fanout_queue_order";
    public static final String EXCHANGE_FANOUT = "exchange_fanout_order";
    public static final String DEAD_LETTER_QUEUE = "dead_letter_queue_order";
    public static final String DEAD_LETTER_EXCHANGE = "dead_letter_exchange_order";
    public static final String DEAD_LETTER_ROUTING_KEY = "dead.key";


    // 原队列消费者：不主动处理消息，仅让消息在队列中等待TTL过期
//    @RabbitListener(
//            bindings = @QueueBinding(
//                    value = @Queue(
//                            value = QUEUE_NAME,
//                            durable = "true",
//                            arguments = {
//                                    @Argument(name = "x-dead-letter-exchange", value = DEAD_LETTER_EXCHANGE),
//                                    @Argument(name = "x-dead-letter-routing-key", value = DEAD_LETTER_ROUTING_KEY),
//                                    @Argument(name = "x-max-length", value = "1000", type = "java.lang.Long"),
//                                    @Argument(name = "x-message-ttl", value = "60000", type = "java.lang.Long") // 队列级TTL：60秒后过期
//                            }
//                    ),
//                    exchange = @Exchange(
//                            value = EXCHANGE_FANOUT,
//                            type = ExchangeTypes.FANOUT,
//                            durable = "true"
//                    )
//            ),
//            ackMode = "MANUAL" // 手动确认模式（关键：不自动确认消息）
//    )
//    public void processMessageBack(Order order, Message message, Channel channel) throws IOException {
//        try {
//            log.info("原队列接收消息（等待过期），orderId:{}，设置的过期时间:{}ms",
//                    order != null ? order.getId() : "未知",
//                    message.getMessageProperties().getExpiration());
//
//            // 关键：不做任何确认/拒绝操作，让消息保持在Unacked状态，等待TTL过期
//            // 消息会在原队列中等待设置的TTL时间，到期后自动转为死信进入死信队列
//
//        } catch (Exception e) {
//            log.error("原队列消息处理异常，强制确认消息（避免消息卡住）", e);
//            // 异常情况下手动确认消息，防止消息一直卡在Unacked状态
//            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//        }
//    }


    // 死信队列消费者：处理延迟后的业务逻辑
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEAD_LETTER_QUEUE, durable = "true"),
                    exchange = @Exchange(value = DEAD_LETTER_EXCHANGE, type = ExchangeTypes.DIRECT, durable = "true"),
                    key = DEAD_LETTER_ROUTING_KEY
            ),
            ackMode = "MANUAL" // 显式手动确认
    )
    public void processDeadLetter(Order order, Message message, Channel channel) throws IOException {
        try {
            // 校验Order有效性
            if (order == null || order.getId() == null) {
                log.error("死信消息无效：Order为空或ID缺失，消息ID:{}", message.getMessageProperties().getMessageId());
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            // 处理业务逻辑
            log.info("开始处理死信消息，orderId:{}", order.getId());
            orderService.orderCancelConsumer(order);

            // 处理成功，确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.info("死信消息处理完成，orderId:{}", order.getId());
        } catch (Exception e) {
            log.error("处理死信消息失败，orderId:{}", order.getId(), e);
            // 确认丢弃，避免无限循环
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

}
