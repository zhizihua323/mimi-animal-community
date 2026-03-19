package com.example.coreshopping.dto;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.mimi.shopping.entity.Goods;

@Mapper
public interface GoodsDto extends BaseMapper<Goods> {
}
