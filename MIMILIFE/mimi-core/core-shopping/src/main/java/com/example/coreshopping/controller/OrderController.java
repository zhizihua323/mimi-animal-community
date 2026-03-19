package com.example.coreshopping.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.coreshopping.service.GoodsService;
import com.example.coreshopping.service.OrderService;
import com.example.coreshopping.utils.OrderEnum;
import com.example.coreshopping.utils.producer.OrderProducer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.checkerframework.checker.units.qual.A;
import org.mimi.entity.User;
import org.mimi.redis.utils.RedisConstant;
import org.mimi.result.Result;
import org.mimi.shopping.entity.Goods;
import org.mimi.shopping.entity.Order;
import org.mimi.threadLocal.UserContextThreadLocal;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/shopping/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    @GetMapping
    public Result<List<Order>> pageOrderList(){
        User user = UserContextThreadLocal.getUser();

        LambdaQueryWrapper<Order> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(user.getId() != null,Order::getUserId,user.getId());

        List<Order> orderList = orderService.list(lambdaQueryWrapper);

        return Result.ok(orderList);
    }

    @GetMapping("/getOne")
    public Result<Order> getOrderById(@RequestParam("id") Long id){
        User user = UserContextThreadLocal.getUser();

        Order order = orderService.getById(id);
        if(!Objects.equals(user.getId(), order.getUserId())){
            return Result.error("没有权限");
        }

        return Result.ok(order);
    }

    @PostMapping
    public Result createInitOrder(@RequestBody Order order) {



        if (order.getGoodsId() == null) {
            return Result.error("未传递货物id");
        }

        User user = UserContextThreadLocal.getUser();
        String openid = user.getOpenid();
        Long userId = user.getId();
        OrderEnum orderEnum = OrderEnum.PAY_STATUS_UNPAID;

        Long goodsId = order.getGoodsId();
        Goods goods= goodsService.getById(goodsId);

        order.setStatus(orderEnum.getCode());
        if(order.getNum() == null){
            order.setNum(1);
        }
        order.setUserOpenid(openid);
        order.setUserId(userId);
        order.setOrderTime(LocalDateTime.now());
        order.setPrice(goods.getPrice());
        order.setFreight(BigDecimal.TEN);
        order.setGoodsUrl(goods.getPicUrl());
// 保存订单
        boolean saveSuccess = orderService.save(order);

        log.info("order的信息为{}",order);



        rabbitTemplate.convertAndSend(
                OrderProducer.EXCHANGE_FANOUT,
                "",  // fanout交换机无需路由键
                order,
                message -> {
                    // 1. 修复contentType：正确设置为application/json
                    message.getMessageProperties().setContentType("application/json");
                    // 2. 设置消息过期时间（延迟10秒，根据需求调整）
                    message.getMessageProperties().setExpiration("60000");
                    // 3. 消息持久化（可选，确保RabbitMQ重启不丢消息）
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                }
        );




        if (saveSuccess) {
            // 保存成功后，order对象的id字段已自动填充生成的主键值
            Long orderId = order.getId();
            // 可以将orderId返回给前端，用于后续操作（如支付跳转）
            return Result.ok(order);
        } else {
            return Result.error("订单创建失败");
        }
    }

//    public Boolean updateGoodsStone(String id){
//        String cacheKey = RedisConstant.ORDER_STOCK_KEY_NX + id;
//        String token = UUID.randomUUID().toString();
//
//        Boolean acquireLock = redisTemplate.opsForValue().setIfAbsent(cacheKey,token,3, TimeUnit.SECONDS);
//        if( Boolean.TRUE.equals(acquireLock)){
//            Goods goods = goodsService.getById(id);
//            if(goods.getCollect() > 0){
//                goods.setCollect(goods.getCollect()-1);
//            }
//            String redisToken = (String) redisTemplate.opsForValue().get(cacheKey);
////            if (token.equals(redisToken)) {
////                redisTemplate.delete(cacheKey);
////            }
//            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
//            redisScript.setScriptText(RedisConstant.ORDER_STOCK_LUA_SCRIPT);
//            redisScript.setResultType(Long.class);
//
//            redisTemplate.execute(redisScript, Collections.singletonList(cacheKey),token);
//
//
//            return  true;
//        }
//
//        return false;
//    }

    public Boolean updateGoodsStone(String id){

        RLock lock = redissonClient.getLock(RedisConstant.ORDER_STOCK_KEY_NX + id);
        String uuid = UUID.randomUUID().toString();

        try {
            lock.lock();

            //执行核心业务

            //
        }catch (Exception error){

        }finally {
            lock.unlock();
        }




        return false;
    }
}

