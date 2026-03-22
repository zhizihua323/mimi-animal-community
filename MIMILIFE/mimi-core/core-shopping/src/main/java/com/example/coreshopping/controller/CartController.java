package com.example.coreshopping.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.coreshopping.service.CartService;
import org.mimi.entity.User;
import org.mimi.result.Result;
import org.mimi.shopping.entity.Cart;
import org.mimi.threadLocal.UserContextThreadLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shopping/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // 1. 加入购物车
    @PostMapping
    public Result addCart(@RequestBody Cart cart) {
        User user = UserContextThreadLocal.getUser();
        cart.setUserId(user.getId());

        // 🌟 核心逻辑：查询购物车里是否已经有该商品
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, user.getId())
                .eq(Cart::getGoodsId, cart.getGoodsId());

        Cart existCart = cartService.getOne(queryWrapper);

        if (existCart != null) {
            // 如果存在，数量直接相加
            existCart.setNum(existCart.getNum() + (cart.getNum() != null ? cart.getNum() : 1));
            cartService.updateById(existCart);
        } else {
            // 如果不存在，插入新记录
            if (cart.getNum() == null) cart.setNum(1);
            if (cart.getIsChecked() == null) cart.setIsChecked(1); // 默认选中
            cart.setCreateTime(LocalDateTime.now());
            cartService.save(cart);
        }
        return Result.ok("加入购物车成功");
    }

    // 2. 获取当前用户的购物车列表
    @GetMapping
    public Result<List<Cart>> getCartList() {
        User user = UserContextThreadLocal.getUser();
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, user.getId())
                .orderByDesc(Cart::getCreateTime); // 按时间倒序，新加的在上面

        List<Cart> cartList = cartService.list(queryWrapper);
        return Result.ok(cartList);
    }

    // 3. 更新购物车 (用于修改数量、勾选状态)
    @PutMapping
    public Result updateCart(@RequestBody Cart cart) {
        // 安全起见，限制只能修改自己的购物车项
        User user = UserContextThreadLocal.getUser();
        cart.setUserId(user.getId());

        cartService.updateById(cart);
        return Result.ok("更新成功");
    }

    // 4. 删除购物车项 (改为 RequestBody 接收数组，彻底解决传参和精度问题)
    @PostMapping("/delete")
    public Result deleteCart(@RequestBody List<Long> ids) {
        cartService.removeByIds(ids);
        return Result.ok("删除成功");
    }
}