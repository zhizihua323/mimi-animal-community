package com.example.coreshopping.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.coreshopping.dto.CartDto;
import com.example.coreshopping.service.CartService;
import org.mimi.shopping.entity.Cart;
import org.springframework.stereotype.Service;


@Service
public class CartServiceImpl extends ServiceImpl<CartDto, Cart> implements CartService {
}
