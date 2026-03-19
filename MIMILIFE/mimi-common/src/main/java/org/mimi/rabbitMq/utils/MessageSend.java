package org.mimi.rabbitMq.utils;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageSend {

    public static final String EXCHANGE_DIRECT = "exchange.direct.order";
    public static final String ROUTING_KEY = "order";
    public static final String QUEUE_NAME = "queue.order";
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void SendMessageTest(){
        rabbitTemplate.convertAndSend(EXCHANGE_DIRECT,ROUTING_KEY,"HELLO RABBITMQ");
    }

}
