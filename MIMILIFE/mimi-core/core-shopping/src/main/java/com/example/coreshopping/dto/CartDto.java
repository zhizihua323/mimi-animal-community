package com.example.coreshopping.dto;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.mimi.shopping.entity.Cart;

/**
 * @description: 购物车
 * @author: zhizihua
 * @Email: 2876290193@qq.com
 * @date: 2026/3/19
 */

@Mapper
public interface CartDto extends BaseMapper<Cart> {
}
