package com.example.coreshopping.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.coreshopping.dto.GoodsDto;
import com.example.coreshopping.service.GoodsService;
import org.mimi.shopping.entity.Goods;
import org.springframework.stereotype.Service;


@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsDto, Goods> implements GoodsService {
}
