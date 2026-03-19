package org.mimi.rabbitMq.utils;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class MessageListener {

    public static final String EXCHANGE_DIRECT = "exchange.direct.order";
    public static final String ROUTING_KEY = "order";
    public static final String QUEUE_NAME = "queue.order";





    //这样子声明可以在监听的时候顺便创建队列，交换机，及其绑定关系
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(value = QUEUE_NAME,durable = "true"),
//            exchange = @Exchange(value = EXCHANGE_DIRECT),
//            key = {ROUTING_KEY}
//        )
//    )

    //这样子就是直接监听队列
    @RabbitListener(queues = {QUEUE_NAME})
    public void processMessage(String dataString, Message message, Channel channel) throws IOException {
        Long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {

            channel.basicAck(deliveryTag,false);
        }catch (Exception error){
            Integer count = message.getMessageProperties().getMessageCount();

            //第三个参数的意思是
            //失败后是否重写投递回消息队列
            //如果为false,就不重写放回队列，broker 会丢弃这个消息
            channel.basicNack(deliveryTag,false,true);
        }
        System.out.println(dataString);
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = QUEUE_NAME,durable = "true"),
            exchange = @Exchange(value = EXCHANGE_DIRECT,type = ExchangeTypes.FANOUT,durable = "true")
        )
    )
    public void processMessageBack(String dataString, Message message, Channel channel){
        System.out.println(dataString);
    }

}
