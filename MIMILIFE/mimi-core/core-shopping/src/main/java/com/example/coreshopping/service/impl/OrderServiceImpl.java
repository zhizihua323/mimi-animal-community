package com.example.coreshopping.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.coreshopping.dto.OrderDto;
import com.example.coreshopping.service.OrderService;
import org.mimi.shopping.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderDto, Order> implements OrderService {

    @Autowired
    private OrderDto orderDto;

    @Override
    public void orderCancelConsumer(Order order) {
        order.setIsExpired(1);

        orderDto.updateById(order);

    }
}
