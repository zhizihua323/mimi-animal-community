package com.example.coreshopping.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mimi.shopping.entity.Order;
import com.example.coreshopping.service.OrderService;
import org.mimi.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/admin/order")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    // 1. 分页查询订单
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       Integer status) {
        Page<Order> page = new Page<>(current, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, Order::getStatus, status);
        wrapper.orderByDesc(Order::getOrderTime);
        return Result.ok(orderService.page(page, wrapper));
    }

    // 2. 修改订单状态 (核心用于“发货”操作)
    @PutMapping("/update")
    public Result update(@RequestBody Order order) {
        return Result.ok(orderService.updateById(order));
    }
}