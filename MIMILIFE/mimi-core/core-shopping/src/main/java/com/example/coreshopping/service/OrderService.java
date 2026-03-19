package com.example.coreshopping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.mimi.shopping.entity.Order;

public interface OrderService extends IService<Order> {
    public void orderCancelConsumer(Order order);

}
